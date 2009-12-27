(ns clj-html.helpers
  (:use (clojure.contrib def)))

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(defvar h escape-html
  "Alias for escape-html")

(defvar doctype
  {:html4
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"
    \"http://www.w3.org/TR/html4/strict.dtd\">\n"

   :xhtml-strict
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"

   :xhtml-transitional
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"}
  "Map of doctype symbols to corresponding declaration strings.")

(defn include-js
  "Include a list of external javascript files."
  [& scripts]
    (for [script scripts]
      [:script {:type "text/javascript" :src script} ""]))

(defn include-css
  "Include a list of external stylesheet files."
  [& styles]
  (for [style styles]
    [:link {:type "text/css" :href style :rel "stylesheet"}]))

(defvar- mime-type-strs
  {:rss  "application/rss+xml"
   :atom "application/rss+xml"})

(defn auto-discovery-link
  "Returns an asset auto discovery tag to make browsers aware of e.g. rss feeds.
  feed-type should be one of :rss or :atom.
  Options: :title :rel :href."
  [feed-type & [opts]]
  [:link {:rel   (or (:rel  opts) "alternate")
          :type  (or (:type opts) (mime-type-strs feed-type))
          :title (or (:tile opts) (.toUpperCase (name feed-type)))
          :href  (:href opts)}])

(defn browser-method?
  "True iff the method is :get or :post."
  [method]
  (or (= method :get) (= method :post)))

(defn text-field
  "A text field.
  Options: :id :class"
  [name & [value & [opts]]]
  [:input (merge opts {:type "text" :name name :value value})])

(defn password-field
  "A password field.
  Options: :id :class"
  [name & [value & [opts]]]
  [:input (merge opts {:type "password" :name name :value value})])

(defn text-area
  "A text area.
  Options: :id :class :rows :cols :readonly :spellcheck."
  [name & [value & [opts]]]
  [:textarea (merge opts {:name name}) value])

(defn hidden-field
  "A hidden field."
  [name value]
  [:input {:type "hidden" :name name :value value}])

(defn file-field
  "A file input field."
  [name]
  [:input {:type "file" :name name}])

(defn submit
  "A submit button with the given text."
  [text]
  [:input {:type "submit" :value text}])

(defn form
  "A form.
  Options: :to :multipart"
  [opts & body]
  (let [[method url] (:to opts)]
    (if (browser-method? method)
      [:form {:method (name method) :action url} body]
      [:form {:method "post" :action url
              :enctype (if (:multipart opts) "multipart/form-data")}
          (cons
            (hidden-field "_method" (name method))
            body)])))

(defn link
  "A link with anchor text to the url.
  Options: :title"
  [text url & [opts]]
  (let [title (get opts :title text)]
    [:a {:href url :title title} text]))

(defn delete-button
  "A form consisting only of a button that, when clicked, will send a delete
  request to the given path."
  [text url]
  (form {:to [:delete url]}
    (submit text)))
