(ns fset.index-test
  (:import [java.util Map])
  (:require
    [clojure.test :refer :all]
    [criterium.core :refer [bench quick-bench quick-benchmark]]
    [fset.core :as fset]))

(deftest key-for-values-test
  (is (= [1] (into [] (.a (fset/abox 1)))))
  (is (= [1 2] (into [] (.a (fset/abox 1 2)))))
  (is (= [1 2 3] (into [] (.a (fset/abox 1 2 3)))))
  (is (= [1 2 3 4] (into [] (.a (fset/abox 1 2 3 4)))))
  (is (= [1 2 3 4 5] (into [] (.a (fset/abox 1 2 3 4 5))))))

(deftest key-from-test
  (let [m {1 1 2 2 3 3 4 4 5 5 6 6}]
    (is (= [1] (into [] (.a (fset/key-from m 1)))))
    (is (= [1 2] (into [] (.a (fset/key-from m 1 2)))))
    (is (= [1 2 3] (into [] (.a (fset/key-from m 1 2 3)))))
    (is (= [1 2 3 4] (into [] (.a (fset/key-from m 1 2 3 4)))))
    (is (= [1 2 3 4 5] (into [] (.a (fset/key-from m 1 2 3 4 5)))))
    (is (= [1 2 3 4 5 6] (into [] (.a (fset/select-keys* m [1 2 3 4 5 6])))))))

(deftest index-from-test
  (let [rel #{{:a 10 :b 2 :c 30 :d 4 :e 50}
              {:a 11 :b 21 :c 31 :d 41 :e 51}
              {:a 12 :b 22 :c 32 :d 42 :e 52}
              {:a 1 :b 2 :c 3 :d 43 :e 53}
              {:a 14 :b 24 :c 34 :d 44 :e 54}
              {:a 1 :b 2 :c 3 :d 45 :e 55}
              {:a 16 :b 26 :c 36 :d 46 :e 56}}]
    (is (= [{:a 10 :b 2 :c 30 :d 4 :e 50}]
           (.get ^Map (fset/index-for rel :b :d) (fset/abox 2 4))))
    (is (= [{:a 1 :b 2 :c 3 :d 45 :e 55} {:a 1 :b 2 :c 3 :d 43 :e 53}]
           (.get ^Map (fset/index-for rel :a :b :c) (fset/abox 1 2 3))
           (.get ^Map (fset/index* rel [:a :b :c]) (fset/abox 1 2 3))))))

(deftest select-keys-test
  (is (= {:a 1 :b 2} (fset/select-keys {:a 1 :b 2 :c 3} [:a :b]))))
