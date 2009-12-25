(ns clj-html.core
  (:use [clojure.contrib.def       :only (defvar- defmacro-)]
        [clojure.contrib.str-utils :only (re-gsub)]
        [clojure.contrib.except    :only (throwf)])
  (:load "core_utils"))

;; Shared by the Interpreter and Compiler

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


;; Interpreter

(defn- closing-tag+
  "Returns an html snippet of a self-closing tag acording to the given tag+
  String and the optionally given attrs Map."
  [tag+ attrs]
  (let [[tag tag-attrs] (parse-tag+-attrs tag+)]
    (str "<" tag (attrs-props tag-attrs) (attrs-props attrs) " />")))

(defn- wrapping-tag+
  "Returns an html snippet of a self-closing tag acording to the given tag+
  String, inner content String, and optionally the given attrs Map."
  [tag+ attrs inner]
  (let [[tag tag-attrs] (parse-tag+-attrs tag+)]
    (str "<" tag (attrs-props tag-attrs) (attrs-props attrs) ">"
         inner "</" tag ">")))

(defn- htmli*
  "Interpret the elem, returning the corresponding string of html."
  [elem]
  (cond
    (vector? elem)
      (let [tag+        (name (first elem))
            tag-args    (next elem)
            maybe-attrs (first tag-args)]
        (if (nil? maybe-attrs)
          (closing-tag+ tag+ {})
          (if (map? maybe-attrs)
            (if-let [body (next tag-args)]
              (wrapping-tag+ tag+ maybe-attrs (htmli* body))
              (closing-tag+ tag+ maybe-attrs))
            (wrapping-tag+ tag+ {} (htmli* tag-args)))))
    (seq? elem)
      (apply str (map htmli* elem))
    :else
      (str elem)))

(defn htmli
  "Returns a string corresponding to the rendered elems."
  [& elems]
  (htmli* elems))


;; Compiler

(defn- literal?
  "Returns true if the given form is an atomic compile-time literal."
  [form]
  (or (string? form)
      (keyword? form)
      (symbol? form)
      (number? form)
      (contains? #{nil false true} form)))

(defn- prepare-tag+-info
  "Returns a tuple of [tag lit-attrs-str dyn-attrs] corresponding to the
  given tag+ String and given-attrs Map."
  [tag+ given-attrs]
  (let [[tag-str tag-attrs]     (parse-tag+-attrs tag+)
         attrs                  (merge tag-attrs given-attrs)
         [lit-attrs dyn-attrs]  (separate-map
                                  #(and (literal? (first %))
                                        (literal? (second %)))
                                  attrs)
         lit-attrs-str          (attrs-props lit-attrs)]
    [tag-str lit-attrs-str dyn-attrs]))

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
    (or (literal? tree) (list? tree) (instance? clojure.lang.Cons tree))
      (list tree)
    (and (vector? tree) (keyword? (first tree)))
      (expand-tag+-tree tree)
    :else
      (throwf "Unrecognized form %s, was a %s" tree (class tree))))

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

(defn flatten-content
  "If content is a seq, recursively flattens it into a single string. Otherwise
  returns it."
  [content]
  (cond
    (vector? content)
      (htmli* content)
    (seq? content)
      (apply str (map flatten-content content))
    (literal? content)
      content
    :else
      (throwf (str "Compilation produced non-literal element: " content))))

(defn- append-code
  "Expands the given form into one that will append the result of evaluating
  the form to the html-builder. Code is added as neccessary to ensure that forms
  that may evaluate to nil at runtime are checked before appending."
  [form]
  (if (literal? form)
    `(.append ~html-builder-sym ~form)
    `(if-let [content# ~form]
       (.append ~html-builder-sym (flatten-content content#)))))

(defmacro html
  "Expands into forms that render the html in an efficient manner."
  [& trees]
  `(let [~html-builder-sym (StringBuilder.)]
     ~@(map append-code (compact (coalesce-strings (mapcat expand-tree trees))))
     (.toString ~html-builder-sym)))

(defmacro defhtml
  "Define a function that uses the html macro to render a template."
  [name args & body]
  `(defn ~name ~args (html ~@body)))
