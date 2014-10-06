(ns clj-ioc.core-test
  (:require [clojure.test :refer :all]
            [clj-ioc.core :refer :all]
            [clj-ioc.demo.indirect :as indirect]))

(deftest human
  (testing "Human..."
    (indirect/set-namespace! "clj-ioc.demo.human")
    (is (.equals "Homo sapien" (indirect/scientific-name)))
    (is (.equals "Hi." (indirect/greet)))
    (is (.equals "Hi." (indirect/hello)) "Called function in the IOC container namespace does not have to be same in the IOC namespace.")
    (is (.equals "Hi, Jake." (indirect/greet "Jake")))))

(deftest dog
  (testing "Dog..."
    (indirect/set-namespace! "clj-ioc.demo.dog")
    (is (.equals "Canis lupus familiaris" (indirect/scientific-name)))
    (is (.equals "Arf." (indirect/greet)))
    (is (.equals "Arf, Jake." (indirect/greet "Jake")))))

(deftest cat
  (testing "Cat..."
    (is (thrown? RuntimeException (indirect/set-namespace! "clj-ioc.demo.cat")) "Should throw an exception is no force? flag and missing functions.")
    (indirect/set-namespace! "clj-ioc.demo.cat" true)
    (is (thrown? RuntimeException (.equals "Felinis non get here-is" (indirect/scientific-name))) "Should thrown an exception if call missing function.")
    (is (.equals "Meow." (indirect/greet)))
    (is (.equals "Meow, Jake." (indirect/greet "Jake")))))
