`clj-html` is a Clojure library for fast and expressive HTML templates

Overview
--------

A Clojure library for expanding expressive markup templates into efficient code, drawing on the functional interface and markup syntax of [compojure's html  library](http://github.com/weavejester/compojure/tree/master) and the compilation approach of [cl-who](http://www.weitz.de/cl-who/).

Examples
--------

A simple template:

    (html
      [:body [:div#content "Hello World"]])
    
    ; expands to:
    (let* [html-builder (StringBuilder.)] 
      (.append html-builder "<body><div id=\"content\">Hello World</div></body>") 
      (.toString html-builder))
    
    ;evaluates to:
    "<body><div id=\"content\">Hello World</div></body>"
    </code></pre>
    
A template with non-literal values:

    (html
      [:body
        [:div#content
          [:h1.greeting greeting]
          [:p.message message]]]))
          
    ; expands to:
    (let* [html-builder (StringBuilder.)] 
      (.append html-builder "<body><div id=\"content\"><h1 class=\"greeting\">") 
      (if-let [content__148 greeting] (.append html-builder content__148)) 
      (.append html-builder "</h1><p class=\"message\">") 
      (if-let [content__148 message] (.append html-builder content__148)) 
      (.append html-builder "</p></div></body>") 
      (.toString html-builder))
    
    ; evaluates to (with greeting bound to "Hello" and message to "from clj-html"):
    "<body><div id=\"content\"> \
     <h1 class=\"greeting\">Hello</h1> \
     <p class=\"message\">from clj-html</p> \
     </div></body>"
     </code></pre>

A more involved template:

    (html
      [:body
        [:div#examples
          [:p.string "foo"]
          [:p.literal 3]
          [:p.expression (str "clj" "-" "html")]
          [:ul#sequence
            (domap-str [char '(a b c)]
              (html [:li char]))]]])
    
    ; expansion omitted, evaluation as expected.

Details - Core
--------------

The primary entry point into the `clj-html.core` library is the macro `html`, which expands into code that will render the given template, returning a string. @html@ accepts a vararg list of forms,each of which will be expanded according to the following rules:

Any atom (string, number, etc.) or list (code) will be evaluated as-is. If it logically false then nothing is added to the html, otherwise the result is coerced to a string and added to the html output.

A vector with a keyword as its first element will be treated as markup tag  syntax. A tag can use CSS syntax to declare id and/or classes (`:div#myid.myclass`). 

The second element in the vector is an optional literal map of additional attributes to add to the tag (`[:link {:rel "stylesheet" :href "style.css"}]`). The keys must be literal keywords, though the values can be either literal values or expressions to be computed during evaluation. If the value for a key is logically false, no text is added for that key/value pair. If the value is  equal to @true@ then the `"attrname=\"attrname\""` convention is used.

The remaining values in the tag vector are considered the inner content of the  tag and are expanded recursively.

If no inner forms are given (`[:br]`]) then the tag that is created is self-closing (`<br />`). Otherwise a wrapping tag is created.

`clj-html.core` also includes the `htmli`, accepts very similar arguments to `html` but operates as an interpreter instead of a compiler.  For a discussion of the tradeoffs between these two, see "this Gist":http://gist.github.com/45136.

Details - Utils
---------------

`clj-html.utils` provides general helper methods that are useful for a variety of templating tasks, mostly for use with the `html` macro.

The function `map-str` is the usual `map` with a call to `(apply str ...)` in front. This is useful for rendering a sub-template for each element of a collection:

    (defn person-template [person]
      (html
        [:div.person {:id (:id person)}
          [:p.name (:name person)]
          [:p.city (:city person)]]))
    
    (html
      [:div#people
        (map-str person-template people)])

The macro `domap-str` is useful for rendering an inline snippet for each element of a collection. `domap-str` has semantics like `map-str` and a syntax like `doseq`. Note that since the @html@ macro does not reach within code, if you need to use the literal vector syntax within a `domap-str` body you will need to use `html` again.

    (html
      [:div#people
        (domap-str [person people]
          (html [:div.person {:id (:uid person)}
                  [:p.name (:name person)]
                  [:p.city (:city person)]]))])

Also included are several methods of the form `*-html*`. These are designed to reduce the need for eg:

    (html
      [:div
        (when urgent 
          (html
            [:h3 "Urgent!"]))])
    
    ; with when-html becomes
    (html
      [:div
        (when-html urgent
          [:h3 "Urgent!"])])

Currently we have `if-html`, `when-html`, `when-let-html`, and `for-html`.

Finally, you can use `defhtml` to define methods that are html templates without needing to include the outer @html@ manually:

    (defhtml message [text]
      [:div#message
        [:p.text text]])

Dependencies
------------

Include recent versions of Clojure and Clojure Contrib in your classpath.

The test suite uses `clj-unit`, though you won't need to use the library in general.

License
-------

Copyright 2009 Mark McGranaghan and released under an MIT license.