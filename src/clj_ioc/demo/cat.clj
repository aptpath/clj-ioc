(ns clj-ioc.demo.cat)

(defn greet
  ([] (greet nil))
  ([n] (str "Meow" (if n (str ", " n) "") ".")))

