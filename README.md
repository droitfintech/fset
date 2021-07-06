# fset - Fast set/relational library

`fset` is a faster implementation of the functions in Clojure [core.set](https://clojure.github.io/clojure/#clojure.set).

All functions in `fset` are as compatible as possible with `clojure.set`. There are minor differences, for example requiring input arguments to actually implement `IPersistentSet` (while `clojure.set` also allows other collection types as input with [unpredictable results](https://clojuredocs.org/clojure.set/union#example-5b5a7837e4b00ac801ed9e2e), so this is sort of a feature). The library uses [generative tests](https://github.com/droitfintech/fset/blob/main/test/tech/droit/fset_test.clj) to verify compatibility with core functions (run with `lein test`).

The library also includes functions with a different interface to those in `clojure.set` to further improve speed. These functions are clearly identified by ending with "*". For example, `fset` contains both `index` and `index*`: `index` is already faster than `clojure.set/index` and has the same interface. `fset/index*` is even faster than `fset/index` but it breaks compatibility with `clojure.set/index` by returning a mutable `java.util.HashMap` instead of `clojure.lang.PersistentHashMap`.

`fset` also implements other non-set related functions from `clojure.core` when they are instrumental to improve performance for those in `clojure.set` (this is the case of `fset/select-keys` for example).

## Usage

Add `[tech.droit/fset "0.1.1"]` to your dependencies and then:

```clojure
(require '[tech.droit.fset :as fset]
         '[clojure.set :as cset])

(def s1 (set (range 10)))
(def s2 (set (range 20)))

(= (cset/intersection s1 s2)
   (fset/intersection s1 s2))
;; true
```

## Perf showcase

Some comparison with `clojure.core/set` functions as measured in `test/fset/bench.clj` with the [Criterium](https://github.com/hugoduncan/criterium) library. Smaller numbers mean faster computation. All results below are obtained using small sets (of 10-100 items), but the improvement should be bigger on larger sets.

### Set functions

Union (~46% speedup):

```clojure
1.80E-6 cset
0.96E-6 fset (union)
```

Intersection (~43% speedup):

```clojure
7.4855E-6 cset
4.2227E-6 intersection (compatible)
```

Difference (~43% speedup):

```clojure
7.33E-6 cset
4.13E-6 difference
```

Subset? (~80% speedup):

```clojure
6.95E-7 cset
1.48E-7 fset
```

Superset? (~80% speedup):

```clojure
7.11E-7 cset
1.49E-7 fset
```

### Relation functions

Relation functions operate on sets of maps, where all maps have the same set of keys.

Select (~25% speedup):

```clojure
2.44E-6 cset
1.85E-6 fset
```

Project (~50% speedup):

```clojure
1.03E-5 cset
0.55E-5 fset
```

Join (~70% speedup):

```clojure
2.77E-5 cset
0.79E-5 fset
```

Index (~33% speedup):

```clojure
1.37E-5 cset
0.91E-5 fset
```

### Other functions

`clojure.set/rename-keys` (~38% speedup)

```clojure
3.00E-6 cset
1.88E-6 fset
```

`fset/maps` (map over sets). There is no corresponding single function in `clojure.set` for this, but closest `clojure.core` expression is shown below (~42% speedup):

```clojure
(def xs (set (range 1000)))
2.72E-4 (into #{} (map inc xs))
1.60E-4 (fset/maps inc xs)

```

`clojure.core/select-keys` has been re-implemented in `fset` (~ 55% speedup):

```clojure
2.48E-6 core
1.09E-6 fset (select-keys)
```

`kset` is also new: given a relation (a set of maps with the same keys) find the set of keys repeating in each map:

```clojure
6.31E-7 (set (keys (first rel)))
3.57E-7 (fset/kset rel)
```

## Contributors

Many thanks to:

* [Dmitry Zhus](https://github.com/dzhus) generative testing suite

## License

Copyright © 2021 Droit Financial Technologies

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.
