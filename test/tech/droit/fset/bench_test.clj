(ns tech.droit.fset.bench-test
  (:require
    [clojure.test :refer :all]
    [criterium.core :refer [bench quick-bench quick-benchmark benchmark]]
    [tech.droit.fset :as fset]
    [clojure.set :as cset]))

(defmacro b [expr] `(first (:mean (quick-benchmark ~expr {}))))

(deftest ^:bench rename-keys-bench
  (let [m (zipmap (range 100) (range 100))
        kmap {4 40 14 140 45 450 51 510 60 600 69 690 90 900}]
    (is (= (cset/rename-keys m kmap) (fset/rename-keys m kmap)))
    ; 3.006680459548847E-6 cset
    ; 1.8890617169285616E-6 fset (rename-keys)
    (is
      (nil?
        (println
          (b (cset/rename-keys m kmap))
          (str "cset\n" (b (fset/rename-keys m kmap)) " fset (rename-keys)\n"))))))

(deftest ^:bench maps-bench
  (let [xrel '#{{d d1 c c2 a a3 b b2} {d d2 c c1 a a3 b b3}
                {d d2 c c2 a a2 b b3} {d d2 c c3 a a2 b b3}
                {d d3 c c3 a a3 b b2} {d d3 c c2 a a1 b b2}
                {d d3 c c3 a a2 b b1} {d d3 c c1 a a3 b b1}}]
    (is (= (into #{} (map inc (set (range 100)))) (fset/maps inc (set (range 100)))))
    (is (= (into #{} (map #(assoc % :new 0) xrel)) (fset/maps #(assoc % :new 0) xrel)))
    ; 2.720073097731239E-4 core map over set
    ; 1.6059608771929826E-4 fset (maps)
    (is
      (nil?
        (println
          (let [s (set (range 1000))]
            (str (b (into #{} (map inc s)))
                 " core map over set \n"
                 (b (fset/maps inc s))
                 " fset (maps)\n")))))
    ; 5.3075209319225114E-6 core into set
    ; 3.256621217417093E-6 fset (maps)
    (is
      (nil?
        (println
          (let [f #(assoc % :new 0)]
            (b (into #{} (map f xrel)))
            (str "core into set\n" (b (fset/maps f xrel)) " fset (maps)\n")))))))

(deftest ^:bench select-keys-test
  (let [m (zipmap (range 100) (range 100 200))
        ks (range 0 100 10)]
    (is (= (select-keys m ks) (fset/select-keys m ks)))
    (is (= (select-keys m [10 30 50]) (fset/select-key m 10 30 50)))
    ; 2.481968165076176E-6 core
    ; 1.0907595441876438E-6 fset (select-keys)
    (is
      (nil?
        (println
          (b (select-keys m ks))
          (str " core\n" (b (fset/select-keys m ks)) " fset (select-keys)\n"))))
    ; 8.155316297166944E-7 core
    ; 4.1558029460823247E-7 fset (select-key)
    (is
      (nil?
        (println
          (b (select-keys m [10 30 50]))
          (str "core\n" (b (fset/select-key m 10 30 50)) " fset (select-key)\n"))))))

(deftest ^:bench union-bench
  (let [s1 '#{{d d1 c c2 a a3 b b2} {d d2 c c1 a a3 b b3}
              {d d2 c c2 a a2 b b3} {d d2 c c3 a a2 b b3}}
        s2 '#{{d d3 c c3 a a3 b b2} {d d3 c c2 a a1 b b2}
              {d d3 c c3 a a2 b b1} {d d3 c c1 a a3 b b1}}
        s3 '#{{d d3 c c9 a a1 b b2} {d d3 c c3 a a21 b b1}
              {d d3 c c11 a a3 b b1}}
        s4 '#{{d d3 c c9 a a1 b b2} {d d3 c c3 a a21 b b1}
              {d d3 c c11 a a3 b b1} {d d10} {c c14}}
        s5 '#{{d d3 c c9 a a1 b b2} {d d3 c c3 a a21 b b1}
              {d d3 c c11 a a3 b b1} {d d10} {c c14} {a a21} {a a22}}]
    (is (= (cset/union s1 s2) (fset/union s1 s2)))
    ; 2.072722684685433E-6 cset
    ; 1.336075779224572E-6 fset
    (is (nil? (println (b (cset/union s1 s2)) (str "cset\n" (b (fset/union s1 s2)) " fset (union)\n"))))
    ; (is (nil? (println (b (cset/union s1 s2 s3)) (str "cset\n" (b (fset/union s1 s2 s3)) " fset\n"))))
    ; (is (nil? (println (b (cset/union s1 s2 s3 s4)) (str "cset\n" (b (fset/union s1 s2 s3 s4)) " fset\n"))))
    ; (is (nil? (println (b (cset/union s1 s2 s3 s4 s5)) (str "cset\n" (b (fset/union s1 s2 s3 s4 s5)) " fset\n"))))
    ; (is (nil? (println (b (cset/union s1 s2 s3 s4 s5 #{})) (str "cset\n" (b (fset/union s1 s2 s3 s4 s5 #{})) " fset (union)\n"))))
    ))

(deftest ^:bench index-bench
  ; 1.3768563343788355E-5 cset
  ; 0.9147070550913031E-5 fset (index)
  (let [xrel '#{{d d1 c c2 a a3 b b2} {d d2 c c1 a a3 b b3}
                {d d2 c c2 a a2 b b3} {d d2 c c3 a a2 b b3}
                {d d3 c c3 a a3 b b2} {d d3 c c2 a a1 b b2}
                {d d3 c c3 a a2 b b1} {d d3 c c1 a a3 b b1}}
        ks '[b d]]
    (is (= (cset/index xrel ks) (fset/index xrel ks)))
    (is
      (nil?
        (println
          (b (cset/index xrel ks))
          (str "cset\n" (b (fset/index xrel ks)) " fset (index)\n"))))))

(deftest ^:bench kset-bench
  (let [rel '#{{d d2 c c1 a a3 b b3}
               {d d2 c c2 a a2 b b3}
               {d d3 c c3 a a2 b b1}
               {d d3 c c1 a a3 b b1}}]
    (is (= (set (keys (first rel))) (fset/kset rel)))
    ; 6.315077613519734E-7 core
    ; 3.577445668244377E-7 fset (kset)
    (is
      (nil?
        (println
          (b (set (keys (first rel))))
          (str "core\n" (b (fset/kset rel)) " fset (kset)\n"))))
    ; 6.226934114810922E-7 core
    ; 3.4987011405747016E-8 fset (kset-native)
    (is
      (nil?
        (println
          (b (set (keys (first rel))))
          (str "core\n" (b (fset/kset-native rel)) " fset (kset-native)\n"))))))

(deftest ^:bench intersection-bench
  (let [s1 (set (range 1 40))
        s2 (set (range 30 80))
        ss1 (apply sorted-set s1)
        ss2 (apply sorted-set s2)]
    (is (= (cset/intersection s1 s2) (fset/intersection s1 s2)))
    (is (= (cset/intersection s1 s2) (fset/intersection* s1 s2)))
    (is (= (cset/intersection ss1 ss2) (fset/intersection* ss1 ss2)))
    ; 6.989154524230326E-6 cset
    ; 3.640919193885645E-6 intersection (compatible)
    (is
      (nil?
        (println
          (b (cset/intersection s1 s2))
          (str "cset\n" (b (fset/intersection s1 s2)) " intersection (compatible)\n"))))
    ; 9.937802765155177E-6 sorted cset
    ; 7.90225522716554E-6 sorted intersection (compatible)
    (is
      (nil?
        (println
          (b (cset/intersection ss1 ss2))
          (str "sorted cset\n" (b (fset/intersection ss1 ss2)) " sorted intersection (compatible)\n"))))
    ; 6.842457065677723E-6 cset
    ; 3.2923806294768446E-6 intersection (native)
    (is
      (nil?
        (println
          (b (cset/intersection s1 s2))
          (str "cset\n" (b (fset/intersection* s1 s2)) " intersection (native)\n"))))))

(deftest ^:bench difference-bench
  (let [s1 (set (range 1 50))
        s2 (set (range 30 800))
        ss1 (apply sorted-set s1)
        ss2 (apply sorted-set s2)]
    (is (= (cset/difference s1 s2) (fset/difference s1 s2)))
    (is (= (cset/difference ss1 ss2) (fset/difference ss1 ss2)))
    ; 6.65E-6 cset
    ; 3.94E-6 fset
    (is
      (nil?
        (println
          (b (cset/difference s1 s2))
          (str "cset\n" (b (fset/difference s1 s2)) " difference\n"))))
    ; 1.4298325121443442E-5 sorted cset
    ; 1.132363716780562E-5 sorted difference
    (is
      (nil?
        (println
          (b (cset/difference ss1 ss2))
          (str "sorted cset\n" (b (fset/difference ss1 ss2)) " sorted difference\n"))))
    ; 5.0338101960004635E-6 cset
    ; 3.4274965213695293E-6 difference
    (is
      (nil?
        (println
          (b (cset/difference s2 s1))
          (str "cset\n" (b (fset/difference s2 s1)) " difference\n"))))))

(deftest ^:bench select-bench
  (let [xrel '#{{d d1 c c2 a a3 b b2} {d d2 c c1 a a3 b b3}
                {d d2 c c2 a a2 b b3} {d d2 c c3 a a2 b b3}
                {d d3 c c3 a a3 b b2} {d d3 c c2 a a1 b b2}
                {d d3 c c3 a a2 b b1} {d d3 c c1 a a3 b b1}}
        ss (apply sorted-set (range 100))
        pred (comp #{'a3} 'a)]
    (is (= (cset/select pred xrel) (fset/select pred xrel)))
    (is (= (cset/select odd? ss) (fset/select odd? ss)))
    ; 1.5927641938447406E-5 sorted cset
    ; 1.4375441919551693E-5 sorted select
    (is
      (nil?
        (println
          (b (cset/select even? ss))
          (str "sorted cset\n" (b (fset/select even? ss)) " sorted select\n"))))
    ; 2.44818399082862E-6 cset
    ; 1.8558549234948711E-6 select
    (is
      (nil?
        (println
          (b (cset/select pred xrel))
          (str "cset\n" (b (fset/select pred xrel)) " select\n"))))))

(deftest ^:bench project-test
  (let [xrel '#{{d d1 c c2 a a3 b b2} {d d2 c c1 a a3 b b3}
                {d d2 c c2 a a2 b b3} {d d2 c c3 a a2 b b3}
                {d d3 c c3 a a3 b b2} {d d3 c c2 a a1 b b2}
                {d d3 c c3 a a2 b b1} {d d3 c c1 a a3 b b1}}
        ks '[a b c]]
    (is (= (cset/project xrel ks) (fset/project xrel ks)))
    (is (= (cset/project xrel ks) (fset/project* xrel 'a 'b 'c)))
    ; 1.0354762891861535E-5 cset
    ; 0.5515608075685698E-5 project
    (is
      (nil?
        (println
          (b (cset/project xrel ks))
          (str "cset\n" (b (fset/project xrel ks)) " project\n"))))
    ; 1.0387998330899229E-5 cset
    ; 6.966055805671281E-6 project (WIP)
    (is
      (nil?
        (println
          (b (cset/project xrel ks))
          (str "cset\n" (b (fset/project* xrel 'a 'b 'c)) " project\n"))))))

(deftest ^:bench join-bench
  ; 2.7759994340847945E-5 cset
  ; 0.7999750663668448E-5 fset (join)
  ; 0.7712902516469042E-5 fset (join with keys)
  (let [xrel '#{{d d2 c c1 a a3 b b3}
                {d d2 c c2 a a2 b b3}
                {d d2 c c3 a a2 b b3}
                {d d3 c c3 a a3 b b2}
                {d d3 c c2 a a1 b b2}
                {d d1 c c2 a a3 b b2}
                {d d3 c c3 a a2 b b1}
                {d d3 c c1 a a3 b b1}}
        yrel '#{{e e1 f f2 g g1 b b1}
                {e e3 f f1 g g2 b b8}
                {e e1 f f2 g g3 b b8}
                {e e3 f f1 g g2 b b3}
                {e e2 f f2 g g3 b b8}
                {e e1 f f3 g g1 b b8}
                {e e2 f f3 g g2 b b8}
                {e e1 f f3 g g1 b b2}
                {e e3 f f3 g g3 b b8}}
        x-keys (fset/kset-native xrel)
        y-keys (fset/kset-native yrel)]
    (is (= (cset/join xrel yrel) (fset/join xrel yrel)))
    (is (= (cset/join xrel yrel) (fset/join xrel yrel x-keys y-keys)))
    (is
      (nil?
        (println
          (b (cset/join xrel yrel))
          (str "cset\n" (b (fset/join xrel yrel)) " fset (join)\n"))))
    (is
      (nil?
        (println
          (b (cset/join xrel yrel))
          (str "cset\n" (b (fset/join xrel yrel x-keys y-keys)) " fset (join with keys)\n"))))))

(deftest ^:bench subset-super-test
  (let [xrel '#{{d d1 c c2 a a3 b b2} {d d2 c c1 a a3 b b3}
                {d d2 c c2 a a2 b b3} {d d2 c c3 a a2 b b3}
                {d d3 c c3 a a3 b b2} {d d3 c c2 a a1 b b2}
                {d d3 c c3 a a2 b b1} {d d3 c c1 a a3 b b1}}
        sub (set (take 4 xrel))]
    (is (= true (cset/subset? sub xrel) (fset/subset? sub xrel)))
    (is (= true (cset/superset? xrel sub) (fset/superset? xrel sub)))
    ; 6.955503406380901E-7 cset
    ; 1.4824609776142325E-7 subset?
    (is
      (nil?
        (println
          (b (cset/subset? sub xrel))
          (str "cset\n" (b (fset/subset? sub xrel)) " subset?\n"))))
    ; 7.119121416749918E-7 cset
    ; 1.4956368832914817E-7 subset?
    (is
      (nil?
        (println
          (b (cset/superset? xrel sub))
          (str "cset\n" (b (fset/superset? xrel sub)) " superset?\n"))))))
