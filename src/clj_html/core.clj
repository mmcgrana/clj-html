(ns clj-html.core
  (:use [clojure.contrib.def       :only (defvar- defmacro-)]
        [clojure.contrib.str-utils :only (re-gsub)]
        [clojure.contrib.except    :only (throwf)])
  (:load "core_utils"))

;; Shared by the Compiler and Interpreter

(defvar- tag+-lexer #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"
  "Lexer for parsing ids and classes out of tag+s")

(defn- parse-tag+-attrs
  "Returns a [tag-str tag-attrs] vector containing the tag String and attrs Map
  parsed out of the given tag+ string."
  [tag+]
  (let [[match tag-str id doted-classes] (re-matches tag+-lexer tag+)
        tag-attrs (if2 id doted-classes
                    {:id id :class (re-gsub #"\." " " doted-classes)}
                    {:id id}
                    {:class (re-gsub #"\." " " doted-classes)}
                    {})]
    [tag-str tag-attrs]))

(defn attrs-props
  "Returns the key=\"value\" string corresponding to the given attrs seq-able,
  which should yield key, value pairs. Does not add text for pairs in which the
  value is logically false."
  [attrs]
  (str
    (reduce
      (fn [#^StringBuilder builder [key val]]
        (if val
          (doto builder (.append " ") (.append (name key)) (.append "=\"")
                        (.append (if (= val true) (name key) val))
                        (.append "\""))
          builder))
      (StringBuilder.)
      attrs)))

;; Compiler

(defn- literal?
  "Returns true if the given form is an atomic compile-time literal."
  [form]
  (or (string? form)
      (keyword? form)
      (number? form)
      (contains? #{nil false true} form)))

(defn- prepare-tag+-info
  "Returns a tuple of [tag lit-attrs-str sorted-dyn-attrs] corresponding to the
  given tag+ String and given-attrs Map."
  [tag+ given-attrs]
  (let [[tag-str tag-attrs]     (parse-tag+-attrs tag+)
         attrs                  (merge tag-attrs given-attrs)
         [lit-attrs dyn-attrs]  (separate-map #(literal? (second %)) attrs)
         lit-attrs-str          (attrs-props (sort lit-attrs))
         sorted-dyn-attrs       (sort-by first dyn-attrs)]
    [tag-str lit-attrs-str sorted-dyn-attrs]))

(defn- expand-closing-tag+
  "Returns flat list of forms to evaluate and append to render a closing tag."
  [tag+ given-attrs]
  (let [[tag-str lit-attrs-str dyn-attrs] (prepare-tag+-info tag+ given-attrs)]
    (if (empty? dyn-attrs)
      (list (str "<" tag-str lit-attrs-str " />"))
      (list (str "<" tag-str lit-attrs-str)
            `(attrs-props (list ~@dyn-attrs))
            " />"))))

(defvar- expand-tree)

(defn- expand-wrapping-tag+
  "Returns flat list of forms to evaluate and append to render a wrapping tag."
  [tag+ given-attrs body]
  (let [[tag-str lit-attrs-str dyn-attrs] (prepare-tag+-info tag+ given-attrs)]
    (if (empty? dyn-attrs)
      `(~(str "<" tag-str lit-attrs-str ">")
        ~@(mapcat #(expand-tree %) body)
        ~(str "</" tag-str ">"))
      `(~(str "<" tag-str lit-attrs-str)
        ~`(attrs-props (list ~@dyn-attrs))
        ~">"
        ~@(mapcat #(expand-tree %) body)
        ~(str "</" tag-str ">")))))

(defn- expand-tag+-tree
  "Returns a flast list of forms to evaluate and append to render any tree."
  [tree]
  (let [tag+           (name (first tree))
        maybe-attrs    (second tree)
        no-attrs-body  (next tree)
        yes-attrs-body (nnext tree)]
    (if (map? maybe-attrs)
      (if (nil? yes-attrs-body)
        (expand-closing-tag+ tag+ maybe-attrs)
        (expand-wrapping-tag+ tag+ maybe-attrs yes-attrs-body))
      (if (nil? no-attrs-body)
        (expand-closing-tag+ tag+ {})
        (expand-wrapping-tag+ tag+ {} no-attrs-body)))))

(defn- expand-tree [tree]
  "Returns a flat list of forms to evaualte and append to render a tree."
  (cond
    (or (not (coll? tree)) (list? tree) (instance? clojure.lang.LazySeq tree))
      (list tree)
    (and (vector? tree) (keyword? (first tree)))
      (expand-tag+-tree tree)
    :else
      (throwf "Unrecognized form %s" tree)))

(defn- coalesce-strings
  "Returns a seq of forms corresponding to the given seq but with adjacenct
  strings concatenated together."
  [forms]
  (when (seq forms)
    (let [x (first forms)]
      (lazy-seq
        (if (string? x)
          (cons (apply str (take-while string? forms))
                (coalesce-strings (drop-while string? forms)))
          (concat (take-while (comp not string?) forms)
                  (coalesce-strings (drop-while (comp not string?) forms))))))))

(defvar- html-builder-sym 'html-builder
  "Symbol used for the local variable holding the StringBuilder that collects
  html output.")

(defn- append-code
  "Expands the given form into one that will append the result of evaluating
  the form to the html-builder. Code is added as neccessary to ensure that forms
  that may evaluate to nil at runtime are checked before appending."
  [form]
  (if (literal? form)
    `(.append ~html-builder-sym ~form)
    `(if-let [content# ~form] (.append ~html-builder-sym content#))))

(defmacro html
  "Expands into forms that render the html in an efficient manner."
  [& trees]
  `(let [~html-builder-sym (StringBuilder.)]
     ~@(map append-code (compact (coalesce-strings (mapcat expand-tree trees))))
     (.toString ~html-builder-sym)))


;; Interpreter

(defn- merge-attrs
  "Combine the tag-attrs and attrs by sorting the merged pairs by name so
  that the output is deterministic."
  [tag-attrs attrs]
  (sort-by first (merge tag-attrs attrs)))

(defn- closing-tag+
  "Returns an html snippet of a self-closing tag acording to the given tag+
  String and the optionally given attrs Map (which need not be sorted)."
  [tag+ attrs]
  (let [[tag tag-attrs] (parse-tag+-attrs tag+)]
    (str "<" tag (attrs-props (merge-attrs tag-attrs attrs)) " />")))

(defn- wrapping-tag+
  "Returns an html snippet of a self-closing tag acording to the given tag+
  String, inner content String, and optionally the given attrs Map (which need
  not be sorted)."
  [tag+ attrs inner]
  (let [[tag tag-attrs] (parse-tag+-attrs tag+)]
    (str "<" tag (attrs-props (merge-attrs tag-attrs attrs)) ">"
         inner "</" tag ">")))

(defvar- html-trees)

(defn- html-tree
  "Returns a snippet of html corresponding to the given tree."
  [tree]
  (if (vector? tree)
    (let [tree-seq    (seq tree)
          tag+        (name (first tree-seq))
          tree-rest   (next tree-seq)
          maybe-attrs (first tree-rest)]
      (if (nil? maybe-attrs)
        (closing-tag+ tag+ {})
        (if (map? maybe-attrs)
          (if-let [body (next tree-rest)]
            (wrapping-tag+ tag+ maybe-attrs (html-trees body))
            (closing-tag+ tag+ maybe-attrs))
          (wrapping-tag+ tag+ {} (html-trees tree-rest)))))
    (str tree)))

(defn- html-trees
  "Returns a snippet of html corresponding to the given trees, for which
  we flatten sequences 1 level deep to allow for easy loop rendering."
  [trees]
  (apply str (map html-tree (flatten1 trees))))

(defn htmli
  "Returns a string corresponding to the rendered trees."
  [& trees]
  (html-trees trees))
