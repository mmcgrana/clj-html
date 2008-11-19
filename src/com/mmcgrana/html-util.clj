(defmacro- if2 
  "2 x 2 if."
  [a-test b-test a-b a-not-b not-a-b not-a-not-b]
  `(if ~a-test
     (if ~b-test
       ~a-b
       ~a-not-b)
      (if ~b-test
        ~not-a-b
        ~not-a-not-b)))

(defn- separate-map
  "Returns two maps, the first for which the entires satisfy (f entry), the 
  second for which the entries do not. The type of the returned maps will
  be the same as the given map."
  [f h]
  [(into (empty h) (filter f h))
   (into (empty h) (remove f h))])

(defn- compact
  "Returns a lazy seq corresponding to the given coll, without nil values"
  [coll]
  (filter #(not (= % nil)) coll))

(defn map-str
  "Like the usual map, but joins the resulting seq of strings together."
  [f coll]
  (apply str (map f coll)))

(defmacro domap-str
  "Map a collection to strings and return the concatination of those strings.
  Uses doseq syntax."
  [[binding-form list] & body]
  `(apply str (map (fn [~binding-form] ~@body) ~list)))
  

