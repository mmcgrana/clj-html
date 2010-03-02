# clj-htm

A Clojure library for expanding expressive HTML templates into efficient code.

## Examples

A basic template

    (use 'clj-html.core)
    
    (html
      [:div#content "Hello World"])
    
    "<div id=\"content\">Hello World</div>"
    
A template with non-literal values:

    (let [greeting "Hello from clj-html"]
      (html
        [:div#content
          [:h1.greeting greeting]]))
          
    "<div id=\"content\"><h1 class=\"greeting\">Hello from clj-html</h1></div>"

A template with sequences:

    (html
      [:ul {:type "letters"}
        (for [char '("a" "b" "c")]
          [:li char])])
    
    "<ul type=\"letters\"><li>a</li><li>b</li><li>c</li></ul>"

A named template:

    (defhtml header [text]
      [:h3 text])
    
    (header "Heading")
    
    "<h3>Heading</h3>"

## Details

The primary entry point into the `clj-html.core` library is the macro `html`, which expands into code that will render the given template, returning a string. `html` accepts a vararg list of forms, each of which will be expanded according to the following rules:

Any atom (string, number, etc.) or list (code) will be evaluated as-is. If it logically false then nothing is added to the html, otherwise the result is coerced to a string and added to the html output.

A vector with a keyword as its first element will be treated as markup tag  syntax. A tag can use CSS syntax to declare an id and/or classes (`:div#myid.myclass`). 

The second element in the vector is an optional literal map of additional attributes to add to the tag (`[:link {:rel "stylesheet" :href "style.css"}]`). Both the keys and values of the map can be either literal values or expressions to be computed during evaluation. If the value for a key is logically false, no text is added for that key/value pair. If the value is  equal to `true` then the `"attrname=\"attrname\""` convention is used.

The remaining values in the tag vector are considered the inner content of the  tag and are expanded recursively.

If no inner forms are given (`[:br]`]) then the tag that is created is self-closing (`<br />`). Otherwise a wrapping tag is created.

Finally, note that `html` expects anything in tag bodies to evaluate to either a string or a seq of strings. Thus the following are valid:


`clj-html.core` also includes the `htmli` function, which accepts very similar arguments to `html` but operates as an interpreter instead of a compiler.  The main use for 'htmli' is when you cannot express the attributes map as a literal map:

    (defn user-helper [attrs user]
      (htmli [:p (merge attrs {:class "user"}) (:name user)]))

This will render correctly only with `htmli` because the attrs map is not literal, i.e. it is produced by a dynamic call to `merge`.


## License

Copyright 2009-2010 Mark McGranaghan and released under an MIT license.