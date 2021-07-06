# Additional Benchmarks

This document collects additional benchmarks achieved by replacing `clojure.set` with `fset` in some popular open source projects.

## Datascript

* Version 1.1.0
* SHA ae62fa6
* openjdk version "11.0.9.1" 2020-11-04

Datascript uses `clojure.set` in a few critical sections and it comes equipped with a ready to run set of benchmarks. Lower means faster.

```
      | add-1 | add-5 | add-all | init | retract-5 | q1  | q2  | q3  | q4   | qpred1 | qpred2 | freeze | thaw
before| 650.2 | 913.1 | 878.3   | 33.4 | 622.2     | 2.4 | 6.2 | 9.6 | 15.0 | 8.8    | 31.1   | 823.1  | 1995.8
after | 658.2 | 901.8 | 861.3   | 28.3 | 665.3     | 2.1 | 6.1 | 9.1 | 14.3 | 9.0    | 30.7   | 833.5  | 1965.7
```

Verdict: interesting.
The most important benchmarks for fset are the query related `q1-q4`, which are consistently faster.
Notes: had to coerce [one instance](https://github.com/tonsky/datascript/blob/4f1af628d5650e0ca0ffd0b6b384941eef2c37fb/src/datascript/query.cljc#L737) of `subset?` with `(set vars)`.

## Riemann

* Version 0.3.7-SNAPSHOT
* SHA 2d590cf
* openjdk version "11.0.9.1" 2020-11-04

[Riemann](https://github.com/riemann/riemann) is a popular distributed monitoring system. It depends on `clojure.set` in some core parts.

```
      | indexing     | expiring
before| 28.775671 ms | 13.143244 ns
after | 26.060983 ms | 14.106172 ns
```

Verdict: inconclusive.
Notes: had to modify benchmarks to use Criterium, as `dotimes` was too unreliable.

## Crux


* Version 1.17.1
* SHA 11fd8257
* openjdk version "11.0.9.1" 2020-11-04

[Crux](https://github.com/juxt/crux) is a graph-oriented bitemporal database written in Clojure. It's query engine uses `clojure.set` mostly for the compilation phase, which is still part of the programming interface although not strictly connected to the execution path. The following are the results of running the query related [benchmarks](https://github.com/juxt/crux/blob/master/crux-bench/src/crux/bench/tpch.clj):

```
      | run-tpch-queries
before| 339841.871333 ms
after | 291307.660398 ms
```

Verdict: interesting.
Notes: running of benchmarks was achieved with the help of the Crux team, but no modifications (other than introducing fset) were necessary.
