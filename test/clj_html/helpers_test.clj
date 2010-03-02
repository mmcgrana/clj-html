(ns clj-html.helpers-test
  (:use clojure.test
        (clj-html core helpers)))

(deftest test-escape-html
  (is (= "foo&amp;" (escape-html "foo&")))
  (is (= "foo&amp;" (h "foo&"))))

(deftest test-browser-method?
  (is (browser-method? :get))
  (is (browser-method? :post))
  (is (not (browser-method? :put)))
  (is (not (browser-method? :delete))))

(deftest test-text-field
  (is (= "<input value=\"3\" name=\"foo[bar]\" type=\"text\" />"
    (html (text-field "foo[bar]" 3)))))

(deftest test-password-field
  (is (= "<input value=\"3\" name=\"foo[bar]\" type=\"password\" />"
    (html (password-field "foo[bar]" 3)))))

(deftest test-text-area
  (is (= "<textarea name=\"foo[bar]\" id=\"area\"></textarea>"
    (html (text-area "foo[bar]" nil {:id "area"})))))

(deftest test-hidden-field
  (is (= "<input type=\"hidden\" name=\"foo[bar]\" value=\"3\" />"
    (html (hidden-field "foo[bar]" 3)))))

(deftest test-file-field
  (is (= "<input type=\"file\" name=\"foo[bar]\" />"
    (html (file-field "foo[bar]")))))

(deftest test-submit
  (is (= "<input type=\"submit\" value=\"foo\" />"
    (html (submit "foo")))))

(deftest test-form
  (is (= "<form method=\"get\" action=\"/foo\">inner1inner2</form>"
    (html (form {:to [:get "/foo"]} "inner1" "inner2"))))
  (is (= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"put\" />inner</form>"
    (html (form {:to [:put "/foo"]} "inner")))))

(deftest test-link
  (is (= "<a href=\"/bar\" title=\"foo\">foo</a>"
    (html (link "foo" "/bar"))))
  (is (= "<a href=\"/bar\" title=\"bat\">foo</a>"
    (html (link "foo" "/bar" {:title "bat"})))))

(deftest test-delete-button
  (is (= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"delete\" /><input type=\"submit\" value=\"Delete\" /></form>"
    (html (delete-button "Delete" "/foo")))))
