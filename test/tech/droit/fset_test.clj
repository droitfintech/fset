(ns tech.droit.fset-test
  (:require
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [clojure.set :as cset]
    [clojure.string :as string]
    [tech.droit.fset :as fset]))

(def gen-v (gen/vector gen/char-alpha 4 20))
(def gen-str (gen/fmap string/join gen-v))
(def gen-sym (gen/fmap symbol gen-str))
(def gen-kw (gen/fmap keyword gen-str))

(defn instance
  [ks]
  (apply gen/hash-map
         (interleave ks
                     (repeatedly
                       #(gen/one-of [gen-str gen-sym gen-kw])))))

(defn relation
  ([] (relation (gen/sample gen-sym)))
  ([ks]
   (gen/set
     (instance ks)
     {:min-elements 2 :max-elements 10})))

(def ^:const rep 100)

(defspec rename-correctness
  rep
  (prop/for-all [r (relation (into [:a :b :c] (gen/sample gen-kw)))]
                (= (cset/rename r {:a 1 :b 2 :c 3})
                   (fset/rename r {:a 1 :b 2 :c 3}))))

(defspec union-correctness
  rep
  (prop/for-all [r1 (relation) r2 (relation)]
                (and
                  (= (cset/union r1 r2) (fset/union r1 r2))
                  (= r1 (cset/union r1 r1) (fset/union r1 r1)))))

(defspec intersection-correctness
  rep
  (prop/for-all [r1 (relation)
                 r2 (relation)]
                (let [instance (first r1)
                      r2 (conj r2 instance)]
                  (= #{instance}
                     (cset/intersection r1 r2)
                     (fset/intersection r1 r2)))))

(defspec difference-correctness
  rep
  (prop/for-all
    [r1 (relation) r2 (relation)]
    (and (= (cset/difference r1 r2) (fset/difference r1 r2))
         (= (cset/difference r2 r1) (fset/difference r2 r1))
         (= #{} (cset/difference r1 r1) (fset/difference r1 r1)))))

(defspec select-correctness
  rep
  (prop/for-all
    [r1 (relation)]
    (let [pred (comp #(> (count %) 3) #(filter keyword? %) vals)]
      (= (cset/select pred r1) (fset/select pred r1)))))

(defspec project-correctness
  rep
  (prop/for-all
    [r1 (relation)]
    (let [ks (random-sample 0.5 (keys (first r1)))]
      (= (cset/project r1 ks) (fset/project r1 ks)))))

(defspec join-correctness
  rep
  (prop/for-all [r1 (relation (into [:a] (gen/sample gen-kw)))
                 r2 (relation (into [:a] (gen/sample gen-kw)))]
                (let [i1 (first r1)
                      i2 (first r2)
                      r1 (conj r1 (assoc i1 :a "join-value"))
                      r2 (conj r2 (assoc i2 :a "join-value"))]
                  (= #{(assoc (merge i1 i2) :a "join-value")}
                     (cset/join r1 r2)
                     (fset/join r1 r2)
                     (fset/join r1 r2 {:a :a})))))

(defspec subset-superset-correctness
  rep
  (prop/for-all
    [xrel (relation)]
    (let [sub (set (take 3 xrel))]
      (and
        (= true (cset/subset? sub xrel) (fset/subset? sub xrel))
        (= true (cset/superset? xrel sub) (fset/superset? xrel sub))))))

(defspec rename-keys-correctness
  rep
  (prop/for-all
    [m (instance (into (range 400) (gen/sample gen-kw)))
     kmap (instance (into (range 30) (gen/sample gen-kw)))]
    (= (cset/rename-keys m kmap) (fset/rename-keys m kmap))))
