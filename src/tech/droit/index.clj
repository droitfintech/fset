(ns tech.droit.index
  (:import
    [java.util Arrays Map HashMap ArrayList]))

;; Wrapper around an object array instance to
;; redefine hasCode and equals to use in hash-map keys.
(deftype ABox [^objects a]
  Object
  (equals [this other]
    (Arrays/equals a ^objects (.a ^ABox other)))
  (hashCode [this]
    (Arrays/hashCode a)))

;; Pretty print ABox
(defmethod print-method tech.droit.index.ABox
  [^ABox abox ^java.io.StringWriter writer]
  (.append writer (str "abox" (into [] (.a abox)))))

(defn abox
  "ABox constructor."
  (^ABox [k1]
   (ABox.
     (doto (object-array 1)
       (aset 0 k1))))
  (^ABox [k1 k2]
   (ABox.
     (doto (object-array 2)
       (aset 0 k1) (aset 1 k2))))
  (^ABox [k1 k2 k3]
   (ABox.
     (doto (object-array 3)
       (aset 0 k1) (aset 1 k2) (aset 2 k3))))
  (^ABox [k1 k2 k3 k4]
   (ABox.
     (doto (object-array 4)
       (aset 0 k1) (aset 1 k2) (aset 2 k3) (aset 3 k4))))
  (^ABox [k1 k2 k3 k4 k5]
   (ABox.
     (doto (object-array 5)
       (aset 0 k1) (aset 1 k2) (aset 2 k3) (aset 3 k4) (aset 4 k5)))))

(defn key-from
  "Alternative to core/select-keys when the number of keys is known. Returns
  an `ABox` instance containing the values corresponding to the keys."
  (^ABox [^Map m k1]
   (abox (.get m k1)))
  (^ABox [^Map m k1 k2]
   (abox (.get m k1) (.get m k2)))
  (^ABox [^Map m k1 k2 k3]
   (abox (.get m k1) (.get m k2) (.get m k3)))
  (^ABox [^Map m k1 k2 k3 k4]
   (abox (.get m k1) (.get m k2) (.get m k3) (.get m k4)))
  (^ABox [^Map m k1 k2 k3 k4 k5]
   (abox (.get m k1) (.get m k2) (.get m k3) (.get m k4) (.get m k5))))

(defn indexer
  "Creates a hashed index to use for join operations. `rel` is a relation
  of maps with uniform keys. `kf` is a function of a map into a native array
  of selected values. Returns a mutable java.util.HashMap with ABox objects as
  keys and vector of maps as values."
  ^HashMap [rel kf]
  (let [out (HashMap.)]
    (loop [items (seq rel)]
      (if items
        (let [item (first items)
              k (kf item)
              ^ArrayList a (if (.containsKey out k) (.get out k) (ArrayList.))]
          (.put out k (doto a (.add item)))
          (recur (next items)))
        out))))

(defn index-for
  "Public interface to `indexer` specialized for different arities."
  (^HashMap [rel k1]
   (indexer rel #(key-from % k1)))
  (^HashMap [rel k1 k2]
   (indexer rel #(key-from % k1 k2)))
  (^HashMap [rel k1 k2 k3]
   (indexer rel #(key-from % k1 k2 k3)))
  (^HashMap [rel k1 k2 k3 k4]
   (indexer rel #(key-from % k1 k2 k3 k4)))
  (^HashMap [rel k1 k2 k3 k4 k5]
   (indexer rel #(key-from % k1 k2 k3 k4 k5))))
