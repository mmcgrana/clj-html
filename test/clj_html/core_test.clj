(ns clj-html.core-test
  (:use clojure.test
        (clj-html [core :only (html htmli defhtml)])))

(defn- in? [coll elem]
  (contains? (set coll) elem))

(defmacro test-html-with
  [label form expected-html]
  (let [label-sym (gensym label)]
    `(deftest ~label-sym
       (is (
         (if (set? ~expected-html) in? =)
         ~expected-html
         ~form)))))

(defmacro test-html [expected-html & body]
  `(do
     (test-html-with "html"  (html ~@body)  ~expected-html)
     (test-html-with "htmli" (htmli ~@body) ~expected-html)))

(test-html
  "<br />"
  [:br])

(test-html
  #{"<br id=\"foo\" class=\"bar\" />" "<br class=\"bar\" id=\"foo\" />"}
  [:br#foo.bar])

(test-html
  "<br class=\"foo bar bat\" />"
  [:br.foo.bar.bat])

(test-html
  "<br id=\"foo\" class=\"bar\" />"
  [:br#foo {:class (str "b" "ar")}])

(test-html
  "<p>inner</p>"
  [:p "inner"])

(test-html
  "<p>3</p>"
  [:p 3])

(test-html
  "<p></p>"
  [:p ""])

(test-html
  "<p class=\"foo\"></p>"
  [:p {:class "foo"} ""])

(test-html
  "<p>3</p>"
  [:p (+ 1 2)])

(test-html
  "<div><p>bar</p><p>bat</p><p>biz</p></div>"
  [:div
    [:p "bar"]
    (if true [:p "bat"])
    [:p "biz"]])

(test-html
  "<div><p>bar</p><p>bat</p><p>biz</p></div>"
  [:div
    [:p "bar"]
    (if true (first (list [:p "bat"])))
    [:p "biz"]])

(test-html
  #{"<p class=\"bar\" id=\"foo\">inner</p>"
    "<p id=\"foo\" class=\"bar\">inner</p>"}
  [:p#foo.bar "inner"])

(test-html
  "<p id=\"foo\" class=\"bar\">inner</p>"
  [:p#foo {:class (str "b" "ar")} "inner"])

(test-html
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class nil} "inner"])

(test-html
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class false} "inner"])

(test-html
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class (if false "bar")} "inner"])

(test-html
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class (number? "bar")} "inner"])

(test-html
  #{"<p attr=\"attr\" id=\"foo\">inner</p>"
    "<p id=\"foo\" attr=\"attr\">inner</p>"}
  [:p#foo {:attr true} "inner"])

(test-html
  "<p id=\"foo\" attr=\"attr\">inner</p>"
  [:p#foo {:attr (number? 3)} "inner"])

(test-html
  "<br a=\"one\" b=\"two\" />"
  [:br {:a "one" :b (str "tw" "o")}])

(test-html
  "<body><div id=\"c\">cont</div><div id=\"f\">foot</div></body>"
  [:body
    [:div#c "cont"]
    [:div#f "foot"]])

(test-html
  "<div><p>high</p><p>a1</p><p>a2</p><p>b1</p><p>b2</p><p>c1</p><p>c2</p><p>low</p></div>"
  [:div
    [:p "high"]
    (for [char '("a" "b" "c")]
      (for [n [1 2]]
        (html [:p char n])))
    [:p "low"]])

(test-html
  "<div><p>high</p><p>a1</p><p>a2</p><p>b1</p><p>b2</p><p>c1</p><p>c2</p><p>low</p></div>"
  [:div
    [:p "high"]
    (for [char '("a" "b" "c")]
      (for [n [1 2]]
        [:p char n]))
    [:p "low"]])

(test-html
  "<div foo-bar=\"foo-bar\"><p>inner</p></div>"
  [:div {(keyword (str "foo" "-" "bar")) true}
    [:p "inner"]])

(deftest test-html-non-nil-variables
  (let [v 3]
    (is (= "<p>3</p>" (html [:p v])))))

(deftest test-html-nil-variables
  (let [v nil]
    (is (= "<p></p>" (html [:p v])))))

(deftest test-html-dynamic-attrs-map-without-inner
  (is (=
    "<br id=\"foo\" />"
    (htmli [:br (hash-map :id "foo")]))))

(deftest test-htmli-dynamic-attrs-map-with-inner
  (is (=
    "<div id=\"foo\"><p>inner</p></div>"
    (htmli [:div (hash-map :id "foo") [:p "inner"]]))))

(defmacro macro-test-helper
  [form]
  `(html (.toUpperCase ~form)))

(test-html
  "INNER"
  (macro-test-helper "inner"))

(deftest test-defhtml
  (defhtml foo [inner] [:div {:num (+ 1 2)} inner])
  (is (= "<div num=\"3\">text</div>" (foo "text"))))
