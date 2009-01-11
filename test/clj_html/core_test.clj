(ns clj-html.core-test
  (:use
    clj-unit.core
    (clj-html [core :only (html htmli)] utils)))

(defmacro test-name [operator expected-html]
  `(apply str ~operator ": " (take 15 ~expected-html)))

(defmacro test-html
  [expected-html & body]
  `(deftest (test-name "html" ~expected-html)
     (assert= ~expected-html (html ~@body))))

(defmacro test-htmli
  [expected-html & body]
  `(deftest (test-name "htmli" ~expected-html)
     (assert= ~expected-html (htmli ~@body))))

(defmacro test-htmlb
  [expected-html & body]
  `(do
     (test-html ~expected-html ~@body)
     (test-htmli ~expected-html ~@body)))

(test-htmlb
  "<br />"
  [:br])

(test-htmlb
  "<br class=\"bar\" id=\"foo\" />"
  [:br#foo.bar])

(test-htmlb
  "<br class=\"foo bar bat\" />"
  [:br.foo.bar.bat])

(test-html
  "<br id=\"foo\" class=\"bar\" />"
  [:br#foo {:class (str "b" "ar")}])
(test-htmli
  "<br class=\"bar\" id=\"foo\" />"
  [:br#foo {:class (str "b" "ar")}])

(test-htmlb
  "<p>inner</p>"
  [:p "inner"])

(test-htmlb
  "<p>3</p>"
  [:p 3])

(test-htmlb
  "<p>3</p>"
  [:p (+ 1 2)])

(test-html
  "<div><p>bar</p><p>biz</p></div>"
  [:div
    [:p "bar"]
    (if false (html [:p "bat"]))
    [:p "biz"]])
(test-htmli
  "<div><p>bar</p><p>biz</p></div>"
  [:div
    [:p "bar"]
    (if false [:p "bat"])
    [:p "biz"]])

(test-htmlb
  "<p class=\"bar\" id=\"foo\">inner</p>"
  [:p#foo.bar "inner"])

(test-html
  "<p id=\"foo\" class=\"bar\">inner</p>"
  [:p#foo {:class (str "b" "ar")} "inner"])
(test-htmli
  "<p class=\"bar\" id=\"foo\">inner</p>"
  [:p#foo {:class (str "b" "ar")} "inner"])

(test-htmlb
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class nil} "inner"])

(test-htmlb
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class false} "inner"])

(test-htmlb
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class (if false "bar")} "inner"])

(test-htmlb
  "<p id=\"foo\">inner</p>"
  [:p#foo {:class (number? "bar")} "inner"])

(test-htmlb
  "<p attr=\"attr\" id=\"foo\">inner</p>"
  [:p#foo {:attr true} "inner"])

(test-html
  "<p id=\"foo\" attr=\"attr\">inner</p>"
  [:p#foo {:attr (number? 3)} "inner"])
(test-htmli
  "<p attr=\"attr\" id=\"foo\">inner</p>"
  [:p#foo {:attr (number? 3)} "inner"])

(test-html
  "<br a=\"one\" d=\"four\" b=\"two\" c=\"three\" e=\"five\" />"
  [:br {:a "one" :b (str "tw" "o") :c (str "thr" "ee")
        :d "four" :e (str "fi" "ve")}])
(test-htmli
  "<br a=\"one\" b=\"two\" c=\"three\" d=\"four\" e=\"five\" />"
  [:br {:a "one" :b (str "tw" "o") :c (str "thr" "ee")
        :d "four" :e (str "fi" "ve")}])

(test-html
  "<body><div id=\"c\">cont</div><div id=\"f\">foot</div></body>"
  [:body
    [:div#c "cont"]
    [:div#f "foot"]])

(test-html
  "<div><p>high</p><p>a</p><p>b</p><p>c</p><p>low</p></div>"
  [:div
    [:p "high"]
    (domap-str [char '(a b c)]
      (html [:p char]))
    [:p "low"]])
(test-htmli
  "<div><p>high</p><p>a</p><p>b</p><p>c</p><p>low</p></div>"
  [:div
    [:p "high"]
    (for [char '(a b c)]
      [:p char])
    [:p "low"]])

(defmacro macro-test-helper
  [form]
  `(html (.toUpperCase ~form)))

(test-html
  "INNER"
  (macro-test-helper "inner"))