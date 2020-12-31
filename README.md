# fset - Fast set/relational library

fset is a re-implementation of the functions in Clojure [core.set](https://clojure.github.io/clojure/#clojure.set) that improves performance of set operations (including relational algebra functions like `join`, `select`, `project` etc.).

All functions are compatible with `clojure.set`. There are minor differences, for example requiring input arguments to actually implement `IPersistentSet` (`clojure.set` would sometimes allow other collection types with unpredictable results, so this is sort of a feature). The library also contains additional functions with a different interface to those in `clojure.set` to further improve speed. These functions have the same name of those in `clojure.set` but ending with "*" to indicate lack of compatibility. For example, `fset` contains both `index` and `index*`: `index` is faster than `clojure.set/index` and has the same interface. `fset/index*` is even faster than `fset/index` but it breaks compatibility with `clojure.set/index` by returning a mutable `java.util.HashMap` instead of `clojure.lang.PersistentHashMap`.

`fset` also implements other non-set related functions from `clojure.core` when they are instrumental to improve performance on `clojure.set` (this is the case of `fset/select-keys` for example).

## Usage

Add `[fset 0.1.0]` to your dependencies and then:

```clojure
(require '[fset.core :as fset]
          [clojure.set :as cset])

(def s1 (set (range 10)))
(def s2 (set (range 20)))

(= (cset/intersection s1 s2)
   (fset/intersection s1 s2))
;; true
```

## Perf showcase

Some comparison with `clojure.core/set` functions as measured in `test/fset/bench.clj` with the [Criterium](https://github.com/hugoduncan/criterium) library.

### Set functions

Union:

```clojure
2.07E-6 cset
1.33E-6 fset
```

Intersection:

```clojure
6.98E-6 cset
3.64E-6 fset
```

Difference:

```clojure
5.03E-6 cset
3.42E-6 fset
```

```clojure
6.95E-7 cset
1.48E-7 subset?
```

```clojure
7.11E-7 cset
1.49E-7 subset?
```

### Relation functions

Relation functions operate on sets of maps, where all maps have the same set of keys.

Select:

```clojure
2.44E-6 cset
1.85E-6 fset
```

Project:

```clojure
1.03E-5 cset
0.55E-5 fset
```

Join:

```clojure
2.77E-5 cset
0.79E-5 fset
```

Index:

```clojure
1.37E-5 cset
0.91E-5 fset
```

### Additional functions

Mapping over sets. There is no corresponding single function in `clojure.set` for this, but closest `clojure.core` expression is shown below:

```clojure
5.30E-6 (into #{} (map #(assoc % :new 0) xrel))
3.25E-6 (fset/maps #(assoc % :new 0) xrel)
```

Core `select-keys` has been re-implemented in `fset`:

```clojure
2.48E-6 core
1.09E-6 fset (select-keys)
```

Given a relation (a set of maps with the same keys) find the set of keys of one of the maps:

```clojure
6.31E-7 (set (keys (first rel)))
3.57E-7 (fset/kset rel)
```

## License

Copyright © 2021 Droit Financial Technologies

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
