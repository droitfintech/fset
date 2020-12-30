# fset - A Fast Set functions implementation library

fset contains a re-implementation of the function in Clojure `core.set` to improve the overall performance of set operations (including relational algebra). Some functions comes with a clojure.set-compatible version and a "native" version that breaks the standard interface to further speed up computation. You can normally just use the compatible interface which has the same name as the relative version in `clojure.set` (for example "intersection"). `fset` also re-implements other non-set related functions from `clojure.core` when they are used by set functions.

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

## License

Copyright © 2020 Droit Financial Technologies

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
