(ns clj-html.utils
  (:use [clj-html.core :only (html)]))

(defmacro defhtml
  "Define a function that uses the html macro to render a template."
  [name args & body]
  `(defn ~name ~args (html ~@body)))

(defmacro if-html
  [test-exp if-body else-body]
  `(if ~test-exp
     (html ~if-body)
     (html ~else-body)))

(defmacro if-let-html
  [[bind-sym test-exp] if-body else-body]
  `(if-let [~bind-sym ~test-exp]
     (html ~if-body)
     (html ~else-body)))

(defmacro when-html
  "Like when, but applying the html macro to the body."
  [test-exp & body]
  `(when ~test-exp (html ~@body)))

(defmacro when-let-html
  "Like when-let, but applying the html macro to the body."
  [[bind-sym test-exp] & body]
  `(when-let [~bind-sym ~test-exp]
     (html ~@body)))
