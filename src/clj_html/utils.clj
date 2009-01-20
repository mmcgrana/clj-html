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
  [[bind-sym list] & body]
  `(apply str (map (fn [~bind-sym] ~@body) ~list)))

(defmacro defhtml
  "Define a function that uses the html macro to render a template."
  [name args & body]
  `(defn ~name ~args (html ~@body)))

(defmacro if-html
  [test if-body else-body]
  `(if ~test
     (html ~if-body)
     (html ~else-body)))

(defmacro when-html
  "Like when, but applying the html macro to the body."
  [test & body]
  `(when ~test (html ~@body)))

(defmacro when-let-html
  "Like when-let, but applying the html macro to the body."
  [[bind-sym test-exp] & body]
  `(when-let [~bind-sym ~test-exp]
     (html ~@body)))

(defmacro for-html
  "Like for, but applying the html macro to the body and concatonating the
  resulting strings together"
  [[bind-sym list] & body]
  `(domap-str [~bind-sym ~list]
     (html ~@body)))
