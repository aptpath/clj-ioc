(ns clj-ioc.demo.human)

(defn greet
  ([] (greet nil))
  ([n] (str "Hi" (if n (str ", " n) "") ".")))

(defn scientific-name
  []
  "Homo sapien")
