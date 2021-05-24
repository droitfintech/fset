(ns tech.droit.fset
  (:refer-clojure :exclude [select-keys])
  (:require [clojure.set :as cset]
            [tech.droit.fset.index :as index])
  (:import
    [java.util Collection Iterator HashSet ArrayList Map HashMap Arrays Set]
    [tech.droit.fset.index ABox]
    [java.lang Iterable]
    [clojure.lang
     ILookup
     IEditableCollection
     ITransientCollection
     PersistentHashSet
     PersistentArrayMap
     PersistentHashMap
     IPersistentSet
     APersistentSet
     APersistentMap
     ITransientMap
     ITransientSet
     PersistentHashMap$NodeIter]))

(def ^{:dynamic true
       :doc "Maximum size for a join operand. This helps controlling large joins
            by throwing error instead of going out of memory."}
  *join-size-threshold* 10000)

(defn- arity-select
  "Help selecting one of the 5 explicit arities available for most
  functions in fset."
  [^Collection items arg f g]
  (let [cnt (.size items)
        ^Iterator it (.iterator items)]
    (case cnt
      1 (f arg (.next it))
      2 (f arg (.next it) (.next it))
      3 (f arg (.next it) (.next it) (.next it))
      4 (f arg (.next it) (.next it) (.next it) (.next it))
      5 (f arg (.next it) (.next it) (.next it) (.next it) (.next it))
      (g arg items))))

(defn rename-keys
  "Like clojure.set/rename-keys, just way uglier and 40% faster."
  [^Map m ^Map kmap]
  (let [^Iterator it (.iterator ^Iterable kmap)
        init (let [^Iterator it (.iterator (.keySet kmap))]
               (loop [^ITransientMap m (.asTransient ^IEditableCollection m)]
                 (if (.hasNext it)
                   (do (.without m (.next it)) (recur m))
                   m)))]
    (loop [^ITransientMap res init]
      (if (.hasNext it)
        (let [^java.util.Map$Entry pair (.next it)
              k (.getKey pair)]
          (when (.containsKey m k)
            (.assoc res (.getValue pair) (.valAt ^ILookup m k)))
          (recur res))
        (.persistent res)))))

(defn maps
  "Like `map` but for sets, returning a set."
  [f ^Iterable s]
  (let [^Iterator items (.iterator s)]
    (loop [out (.asTransient PersistentHashSet/EMPTY)]
      (if (.hasNext items)
        (recur (.conj out (f (.next items))))
        (.persistent out)))))

(defn select-key
  "Like core/select-keys but with specific arities"
  ([m k1]
   (let [out {}
         v1 (clojure.lang.RT/find m k1)]
     (if v1 (conj out v1) out)))
  ([m k1 k2]
   (let [out (.asTransient PersistentArrayMap/EMPTY)
         v1 (clojure.lang.RT/find m k1)
         ^ITransientMap out (if v1 (.conj out v1) out)
         v2 (clojure.lang.RT/find m k2)
         ^ITransientMap out (if v2 (.conj out v2) out)]
     (.persistent out)))
  ([m k1 k2 k3]
   (let [out (.asTransient PersistentArrayMap/EMPTY)
         v1 (clojure.lang.RT/find m k1)
         ^ITransientMap out (if v1 (.conj out v1) out)
         v2 (clojure.lang.RT/find m k2)
         ^ITransientMap out (if v2 (.conj out v2) out)
         v3 (clojure.lang.RT/find m k3)
         ^ITransientMap out (if v3 (.conj out v3) out)]
     (.persistent out)))
  ([m k1 k2 k3 k4]
   (let [out (.asTransient PersistentArrayMap/EMPTY)
         v1 (clojure.lang.RT/find m k1)
         ^ITransientMap out (if v1 (.conj out v1) out)
         v2 (clojure.lang.RT/find m k2)
         ^ITransientMap out (if v2 (.conj out v2) out)
         v3 (clojure.lang.RT/find m k3)
         ^ITransientMap out (if v3 (.conj out v3) out)
         v4 (clojure.lang.RT/find m k4)
         ^ITransientMap out (if v4 (.conj out v4) out)]
     (.persistent out)))
  ([m k1 k2 k3 k4 k5]
   (let [out (.asTransient PersistentArrayMap/EMPTY)
         v1 (clojure.lang.RT/find m k1)
         ^ITransientMap out (if v1 (.conj out v1) out)
         v2 (clojure.lang.RT/find m k2)
         ^ITransientMap out (if v2 (.conj out v2) out)
         v3 (clojure.lang.RT/find m k3)
         ^ITransientMap out (if v3 (.conj out v3) out)
         v4 (clojure.lang.RT/find m k4)
         ^ITransientMap out (if v4 (.conj out v4) out)
         v5 (clojure.lang.RT/find m k5)
         ^ITransientMap out (if v5 (.conj out v5) out)]
     (.persistent out))))

(defn select-keys
  "Like core/select-keys but uses a transient to collect results.
  Note: differently from core/select-keys, it doesn't retain metadata.
  TODO: generative comparison with core/select-keys."
  [m ^Iterable ks]
  (let [^Iterator items (.iterator ks)]
    (loop [out (.asTransient PersistentHashMap/EMPTY)]
      (if (.hasNext items)
        (let [entry (clojure.lang.RT/find m (.next items))]
          (recur (if entry (.conj out entry) out)))
        (.persistent out)))))

(defn union
  "Like core.set/union but with arities optimizations."
  ([] #{})
  ([s1] s1)
  ([s1 s2]
   (let [c1 (count s1) c2 (count s2)
         maxc (max c1 c2)]
     (if (== maxc c1)
       (persistent! (reduce conj! (transient s1) s2))
       (persistent! (reduce conj! (transient s2) s1)))))
  ([s1 s2 s3]
   (let [c1 (count s1) c2 (count s2) c3 (count s3)
         maxc (max c1 c2 c3)]
     (case maxc
       c2 (let [res1 (reduce conj! (transient s2) s1)]
            (persistent! (reduce conj! res1 s3)))
       c3 (let [res1 (reduce conj! (transient s3) s1)]
            (persistent! (reduce conj! res1 s2)))
       (let [res1 (reduce conj! (transient s1) s2)]
         (persistent! (reduce conj! res1 s3))))))
  ([s1 s2 s3 s4]
   (let [c1 (count s1) c2 (count s2) c3 (count s3) c4 (count s4)
         maxc (max c1 c2 c3 c4)]
     (case maxc
       c2 (let [res1 (reduce conj! (transient s2) s1)
                res2 (reduce conj! res1 s3)]
            (persistent! (reduce conj! res2 s4)))
       c3 (let [res1 (reduce conj! (transient s3) s1)
                res2 (reduce conj! res1 s4)]
            (persistent! (reduce conj! res2 s2)))
       c4 (let [res1 (reduce conj! (transient s4) s1)
                res2 (reduce conj! res1 s2)]
            (persistent! (reduce conj! res2 s2)))
       (let [res1 (reduce conj! (transient s1) s2)
             res2 (reduce conj! res1 s3)]
         (persistent! (reduce conj! res2 s4))))))
  ([s1 s2 s3 s4 s5]
   (let [c1 (count s1) c2 (count s2) c3 (count s3) c4 (count s4) c5 (count s5)
         maxc (max c1 c2 c3 c4 c5)]
     (case maxc
       c2 (let [res1 (reduce conj! (transient s2) s1)
                res2 (reduce conj! res1 s3)
                res3 (reduce conj! res2 s4)]
            (persistent! (reduce conj! res3 s5)))
       c3 (let [res1 (reduce conj! (transient s3) s1)
                res2 (reduce conj! res1 s2)
                res3 (reduce conj! res2 s4)]
            (persistent! (reduce conj! res3 s5)))
       c4 (let [res1 (reduce conj! (transient s4) s1)
                res2 (reduce conj! res1 s2)
                res3 (reduce conj! res2 s3)]
            (persistent! (reduce conj! res3 s5)))
       (let [res1 (reduce conj! (transient s1) s2)
             res2 (reduce conj! res1 s3)
             res3 (reduce conj! res2 s4)]
         (persistent! (reduce conj! res3 s5))))))
  ([s1 s2 s3 s4 s5 & sets]
   (let [bubbled-sets (#'cset/bubble-max-key count (conj sets s5 s4 s3 s2 s1))]
     (reduce into (first bubbled-sets) (rest bubbled-sets)))))

(defn kset-native
  "Retrieve the keyset (the attributes) of relation xrel.
  Returns a Java HashSet."
  ^Set [^APersistentSet xrel]
  (let [^PersistentHashMap$NodeIter it (.iterator xrel)
        ^APersistentMap item (when (.hasNext it) (.next it))]
    (if item (.keySet item) #{})))

(defn kset
  "Transforms the result of kset-native into a Clojure set."
  [^APersistentSet xrel]
  (into #{} (kset-native xrel)))

(defn intersection
  "Optimized version of `clojure.set/intersection`. It's mostly compatible,
  but it expects strict sets as arguments (which should be the case). However
  clojure.set/intersection also accepts other types with unpredictable results."
  ([s1] s1)
  ([^IEditableCollection s1 ^IPersistentSet s2]
   (if (< (count s2) (count s1))
     (recur s2 s1)
     (let [^Iterator items (.iterator ^Iterable s1)]
       (loop [^ITransientSet out (.asTransient s1)]
         (if (.hasNext items)
           (let [item (.next items)]
             (if (.contains s2 item)
               (recur out)
               (recur (.disjoin out item))))
           (.persistent out))))))
([s1 s2 & sets]
 (let [bubbled-sets (#'cset/bubble-max-key #(- (count %)) (conj sets s2 s1))]
   (reduce intersection (first bubbled-sets) (rest bubbled-sets)))))

(defn intersection*
  "Like clojure.set/intersection, but returns a java.util.HashSet"
  ([s1] s1)
  (^Set [^Set s1 ^Set s2]
   (if (< (.size s1) (.size s2))
     (doto (HashSet. s1) (.retainAll s2))
     (doto (HashSet. s2) (.retainAll s1))))
  ([s1 s2 & sets]
   (apply intersection s1 s2 sets)))

(defn difference
  "Faster version of clojure.set/difference."
  ([s1] s1)
  ([^PersistentHashSet s1 ^PersistentHashSet s2]
     (if (< (.size s1) (.size s2))
       (let [^Iterator items (.iterator s1)]
         (loop [^ITransientSet out (.asTransient s1)]
           (if (.hasNext items)
             (let [item (.next items)]
               (recur (if (.contains s2 item)
                        (.disjoin out item)
                        out)))
             (.persistent out))))
       (let [^Iterator items (.iterator s2)]
         (loop [^ITransientSet out (.asTransient s1)]
           (if (.hasNext items)
             (recur (.disjoin out (.next items)))
             (.persistent out))))))
  ([s1 s2 & sets]
     (reduce difference s1 (conj sets s2))))

(defn select
  "Faster version of clojure.set/select"
  [pred ^PersistentHashSet s]
  (let [^Iterator items (.iterator s)]
    (loop [^ITransientSet out (.asTransient s)]
      (if (.hasNext items)
        (let [item (.next items)]
          (recur (if (pred item) out (.disjoin out item))))
        (.persistent out)))))

(defn project*
  "Version of clojure.set/project supporting specific arities for keys.
  TODO: work in progress, it doesn't perform better than normal project."
  ([xrel k1] (maps #(select-key % k1) xrel))
  ([xrel k1 k2] (maps #(select-key % k1 k2) xrel))
  ([xrel k1 k2 k3] (maps #(select-key % k1 k2 k3) xrel))
  ([xrel k1 k2 k3 k4] (maps #(select-key % k1 k2 k3 k4) xrel))
  ([xrel k1 k2 k3 k4 k5] (maps #(select-key % k1 k2 k3 k5) xrel)))

(defn project
  "Faster version of clojure.set/project"
  [xrel ks]
  (maps #(select-keys % ks) xrel))

(defn rename
  "Like clojure.set/rename but no meta and optimized. The additional
  arity with k1 k2...kN can be used to rename a known number of keys."
  ([xrel kmap]
   (maps #(rename-keys % kmap) xrel))
  ([xrel k1 k2]
   (maps #(let [v (% k1)]
            (-> %
                (assoc k2 v)
                (dissoc k1)))
         xrel)))

(defn map-invert
  "Returns the map with the vals mapped to the keys."
  {:added "1.0"}
  [m]
  (persistent!
    (reduce
      (fn [m [k v]] (assoc! m v k))
      (transient {})
      m)))

(defn select-keys*
  "Alternative to core/select-keys which retrieves the values corresponding
  to `ks` within an `ABox` instance."
  ^ABox [^Map m ^Iterable ks]
  (let [cnt (count ks)
        out (object-array cnt)
        ^Iterator it (.iterator ks)]
    (loop [i 0]
      (when (.hasNext it)
        (let [succ (.next it)]
          (aset out i (.get m succ))
          (recur (unchecked-inc-int i)))))
    (ABox. out)))

(defn index*
  "Alternative clojure.set/index with a different (but non-compatible)
  interface using native and mutable types."
  ^HashMap [rel ks]
  (index/indexer rel #(select-keys* % ks)))

(defn index
  "Faster clojure.set/index with a compatible interface.
  TODO: generative comparison with set/index."
  [^Set xrel ks]
  (let [^Iterator items (.iterator xrel)]
    (loop [out (.asTransient PersistentHashMap/EMPTY)]
      (if (.hasNext items)
        (let [item (.next items)
              k (arity-select ks item select-key select-keys)]
          (recur (.assoc out k (.cons ^IPersistentSet (.valAt out k #{}) item))))
        (.persistent out)))))

(defn join
  "Optimized version of clojure.set/join about 4x faster."
  ([^Set xrel ^Set yrel]
   (join xrel yrel (kset-native xrel) (kset-native yrel)))
  ([^Set xrel ^Set yrel x-keys y-keys]
   {:pre [(< (count xrel) *join-size-threshold*)
          (< (count yrel) *join-size-threshold*)]}
   (let [xcount (count xrel) ycount (count yrel)]
     (if (and (> xcount 0) (> ycount 0))
       (let [common-keys (intersection* x-keys y-keys)
             <=? (<= xcount ycount)
             r (if <=? xrel yrel)
             s (if <=? yrel xrel)
             ^Map idx (arity-select common-keys r index/index-for index*)]
         (let [^Iterator items (.iterator ^Iterable s)]
           (loop [out (.asTransient PersistentHashSet/EMPTY)]
             (if (.hasNext items)
               (let [item (.next items)]
                 (recur
                   (if-let [found (.get idx (arity-select common-keys item index/key-from select-keys*))]
                     (let [^Iterator joinables (.iterator ^Iterable found)]
                       (loop [out out]
                         (if (.hasNext joinables)
                           (let [^APersistentMap joinable (.next joinables)]
                             (recur (.conj out (.cons joinable item))))
                           out)))
                     out)))
               (.persistent out)))))
       #{})))
  ([xrel yrel km]
   (let [[r s k] (if (<= (count xrel) (count yrel))
                   [xrel yrel (map-invert km)]
                   [yrel xrel km])
         idx (index r (vals k))]
     (.persistent ^ITransientSet (reduce
       (fn [ret item]
         (if-let [found (idx (rename-keys (select-keys item (keys k)) k))]
           (reduce
             (fn [^ITransientCollection acc ^APersistentMap itm]
               (.conj acc (.cons itm item)))
             ret found)
           ret))
       (.asTransient PersistentHashSet/EMPTY) s)))))

(defn subset?
  [^Set set1 ^Set set2]
  (and (<= (.size set1) (.size set2))
       (let [^Iterator items (.iterator set1)]
         (loop []
           (if (.hasNext items)
             (if (.contains set2 (.next items))
               (recur) false)
             true)))))

(defn superset?
  [^Set set1 ^Set set2]
  (and (>= (.size set1) (.size set2))
       (let [^Iterator items (.iterator set2)]
         (loop []
           (if (.hasNext items)
             (if (.contains set1 (.next items))
               (recur) false)
             true)))))
