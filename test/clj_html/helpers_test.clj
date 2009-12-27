(ns clj-html.helpers-test
  (:use (clj-unit core)
        (clj-html core helpers)))

(deftest "escape-html, h"
  (assert= "foo&amp;" (escape-html "foo&"))
  (assert= "foo&amp;" (h "foo&")))

(deftest "browser-method?"
  (assert-fn     browser-method? :get)
  (assert-fn     browser-method? :post)
  (assert-not-fn browser-method? :put)
  (assert-not-fn browser-method? :delete))

(deftest "text-field"
  (assert= "<input value=\"3\" name=\"foo[bar]\" type=\"text\" />"
    (html (text-field "foo[bar]" 3))))

(deftest "password-field"
  (assert= "<input value=\"3\" name=\"foo[bar]\" type=\"password\" />"
    (html (password-field "foo[bar]" 3))))

(deftest "text-area"
  (assert= "<textarea name=\"foo[bar]\" id=\"area\"></textarea>"
    (html (text-area "foo[bar]" nil {:id "area"}))))

(deftest "hidden-field"
  (assert= "<input type=\"hidden\" name=\"foo[bar]\" value=\"3\" />"
    (html (hidden-field "foo[bar]" 3))))


(deftest "file-field"
  (assert= "<input type=\"file\" name=\"foo[bar]\" />"
    (html (file-field "foo[bar]"))))

(deftest "submit"
  (assert= "<input type=\"submit\" value=\"foo\" />"
    (html (submit "foo"))))

(deftest "form"
  (assert= "<form method=\"get\" action=\"/foo\">inner1inner2</form>"
    (html (form {:to [:get "/foo"]} "inner1" "inner2")))
  (assert= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"put\" />inner</form>"
    (html (form {:to [:put "/foo"]} "inner"))))

(deftest "link"
  (assert= "<a href=\"/bar\" title=\"foo\">foo</a>"
    (html (link "foo" "/bar")))
  (assert= "<a href=\"/bar\" title=\"bat\">foo</a>"
    (html (link "foo" "/bar" {:title "bat"}))))

(deftest "delete-button"
  (assert= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"delete\" /><input type=\"submit\" value=\"Delete\" /></form>"
    (html (delete-button "Delete" "/foo"))))