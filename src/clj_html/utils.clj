(ns clj-html.utils
  (:use [clj-html.core :only (html)]))

(defn map-str
  "Map a collection to strings and return the concatination of those strings.
  Uses the clojure.core/map syntax."
  [f coll]
  (apply str (map f coll)))

(defmacro domap-str
  "Map a collection to strings and return the concatination of those strings.
  Uses the clojure.core/doseq syntax."
  [[binding-form list] & body]
  `(apply str (map (fn [~binding-form] ~@body) ~list)))

(defmacro defhtml
  "Define a function that uses the html macro to render a template."
  [name args & body]
  `(defn ~name ~args (html ~@body)))

(defmacro when-html
  "Like when, but apply the html macro to the boyd."
  [test & body]
  `(when ~test (html ~@body)))

(defmacro when-let-html
  "Like when-let, but apply the html macro to the body."
  [[bind-it test-exp] & body]
  `(when-let [~bind-it ~test-exp]
     (html ~@body)))
