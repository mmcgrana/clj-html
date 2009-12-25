`clj-html` is a Clojure library for expanding expressive markup templates into efficient code, drawing on the functional interface and markup syntax of [compojure's html library](http://github.com/weavejester/compojure/tree/master) and the compilation approach of [cl-who](http://www.weitz.de/cl-who/).

Examples
--------

A simple template:

    (html
      [:body [:div#content "Hello World"]])
    
    ; expands to:
    (let* [html-builder (StringBuilder.)] 
      (.append html-builder "<body><div id=\"content\">Hello World</div></body>") 
      (.toString html-builder))
    
    ; evaluates to:
    "<body><div id=\"content\">Hello World</div></body>"
    
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

The primary entry point into the `clj-html.core` library is the macro `html`, which expands into code that will render the given template, returning a string. `html` accepts a vararg list of forms, each of which will be expanded according to the following rules:

Any atom (string, number, etc.) or list (code) will be evaluated as-is. If it logically false then nothing is added to the html, otherwise the result is coerced to a string and added to the html output.

A vector with a keyword as its first element will be treated as markup tag  syntax. A tag can use CSS syntax to declare an id and/or classes (`:div#myid.myclass`). 

The second element in the vector is an optional literal map of additional attributes to add to the tag (`[:link {:rel "stylesheet" :href "style.css"}]`). The keys must be literal keywords, though the values can be either literal values or expressions to be computed during evaluation. If the value for a key is logically false, no text is added for that key/value pair. If the value is  equal to `true` then the `"attrname=\"attrname\""` convention is used.

The remaining values in the tag vector are considered the inner content of the  tag and are expanded recursively.

If no inner forms are given (`[:br]`]) then the tag that is created is self-closing (`<br />`). Otherwise a wrapping tag is created.

Finally, note that `html` expects anything in tag bodies to evaluate to either a string or a seq of strings. Thus the following are valid:

   (html
     (for [n '(1 2 3)]
       (html [:p n])))
    
    (html
      [:div
        (when pred?
          (html [:p "text"]))])

But these will not render with `html`:

    (html
       (for [n [1 2 3]]
          [:p n]))
    
    (html
      [:div
        (when pred?
          [:p "text"])])


`clj-html.core` also includes the `htmli` function, accepts very similar arguments to `html` but operates as an interpreter instead of a compiler.  This gives slightly more flexibility at the cost of increased rendering time. For example, both of the above examples that would fail with `html` will render with `htmli`.

Details - Utils
---------------

`clj-html.utils` provides a few general helper functions for use with the `html` macro.

`defhtml` allows you to define functions that are html templates without needing to include the outer `html` manually:

    (defhtml message [text]
      [:div#message
        [:p.text text]])
        
Also included are several methods of the form `*-html`. These are designed to reduce the need for eg:

    (html
      [:div
        (when urgent 
          (html
            [:h3 "Urgent!"]))])

With `when-html`, this becomes:

    (html
      [:div
        (when-html urgent
          [:h3 "Urgent!"])])

Currently we have `if-html`, `when-html`, `when-let-html`.

License
-------

Copyright 2009 Mark McGranaghan and released under an MIT license.