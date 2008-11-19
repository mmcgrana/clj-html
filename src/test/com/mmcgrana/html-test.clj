(ns test.com.mmcgrana.html
  (:use [com.mmcgrana.html :only (html map-str domap-str)]))

(defmacro test-html
  [expected-html & forms]
  `(let [actual-html# (html ~@forms)]
     (if (= ~expected-html actual-html#)
       (do (print ".") (flush))
       (println (str "\nfailed - expected: " ~expected-html 
                                " but got: " actual-html#)))))

(test-html 
  "<br />" 
  [:br])

(test-html
  "<br class=\"bar\" id=\"foo\" />" 
  [:br#foo.bar])

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
  "<p>3</p>"
  [:p (+ 1 2)])

(test-html
  "<div><p>bar</p><p>biz</p></div>"
  [:div
    [:p "bar"]
    (if false (html [:p "bat"]))
    [:p "biz"]])
                  
(test-html 
  "<p class=\"bar\" id=\"foo\">inner</p>" 
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
  "<p attr=\"attr\" id=\"foo\">inner</p>"
  [:p#foo {:attr true} "inner"])
    
(test-html 
  "<p id=\"foo\" attr=\"attr\">inner</p>"
  [:p#foo {:attr (number? 3)} "inner"])

(test-html 
  "<br a=\"one\" d=\"four\" b=\"two\" c=\"three\" e=\"five\" />"
  [:br {:a "one" :b (str "tw" "o") :c (str "thr" "ee") 
        :d "four" :e (str "fi" "ve")}])

(test-html 
  "<body><div id=\"c\">cont</div><div id=\"f\">foot</div></body>"
  [:body 
    [:div#c "cont"] 
    [:div#f "foot"]])

(defn- ch-partial 
  [ch]
  (html [:p ch]))

(test-html
  "<div><p>a</p><p>b</p><p>c</p><p>d</p></div>"
  [:div
    (map-str ch-partial '(a b c))
    [:p 'd]])

(test-html 
  "<div><p>a</p><p>b</p><p>c</p><p>d</p></div>"
  [:div
    (domap-str [ch '(a b c)]
      (html [:p ch]))
    [:p 'd]])

(println " done")