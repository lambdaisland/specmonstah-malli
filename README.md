# specmonstah-malli

<!-- badges -->
<!-- [![CircleCI](https://circleci.com/gh/lambdaisland/specmonstah-malli.svg?style=svg)](https://circleci.com/gh/lambdaisland/specmonstah-malli) [![cljdoc badge](https://cljdoc.org/badge/lambdaisland/specmonstah-malli)](https://cljdoc.org/d/lambdaisland/specmonstah-malli) [![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/specmonstah-malli.svg)](https://clojars.org/lambdaisland/specmonstah-malli) -->
<!-- /badges -->

Does what Specmonstah does with clojure.spec.alpha, but using Malli instead. WIP.

```
(require '[lambdaisland.specmonstah.malli :as sm-malli]
         '[reifyhealth.specmonstah.core :as sm])

(def schema
  {:user {:prefix :u
          :schema [:map
                   [:foo/id uuid?]
                   [:user/name string?]]}
   :procedure {:prefix    :p
               :schema [:map
                        [:foo/id uuid?]
                        [:procedure/id uuid?]]
               :relations {:procflow.procedure/owner [:user :foo/id]
                           :procflow.procedure/steps [:step :foo/id]}
               :constraints {:procflow.procedure/steps #{:coll :uniq}} }
   :step {:prefix :s
          :schema [:map
                   [:foo/id uuid?]
                   [:step/name string?]]}})

(-> (sm-malli/ent-db-spec-gen {:schema schema}  {:procedure [[3]]
                                                 :step [[10]]})
    (sm/attr-map :spec-gen)
    vals)

;; => ({:foo/id #uuid "6bf6aa0b-edfc-4109-b797-93afc6e012d3",
;;      :procedure/id #uuid "b193342c-9a5f-47ec-9969-369b4dbd384f",
;;      :procflow.procedure/steps [#uuid "db508061-d3c6-45f2-acc1-4808bc401b90"],
;;      :procflow.procedure/owner #uuid "1a037059-865a-4ee1-93cc-47798b116141"}
;;     {:foo/id #uuid "c871b2d5-ea2c-4448-9349-3ebe0ca34758",
;;      :procedure/id #uuid "f19f878d-d240-4e27-a23d-e784fdcb0616",
;;      :procflow.procedure/owner #uuid "1a037059-865a-4ee1-93cc-47798b116141",
;;      :procflow.procedure/steps [#uuid "a3402251-ec16-4a4a-b76e-26a8b979c382"]}
;;     {:foo/id #uuid "43288975-5f44-4fcc-aaf1-fd8db66ade69",
;;      :procedure/id #uuid "83608156-b04a-4c7c-bf57-2584720801b1",
;;      :procflow.procedure/owner #uuid "1a037059-865a-4ee1-93cc-47798b116141",
;;      :procflow.procedure/steps [#uuid "f9b0dda6-ed76-43ff-93a4-3396e3a0927d"]}
;;     {:foo/id #uuid "db508061-d3c6-45f2-acc1-4808bc401b90", :step/name "H"}
;;     {:foo/id #uuid "a3402251-ec16-4a4a-b76e-26a8b979c382", :step/name "f"}
;;     {:foo/id #uuid "f9b0dda6-ed76-43ff-93a4-3396e3a0927d", :step/name "7"}
;;     {:foo/id #uuid "49566f5b-950f-45b2-bd99-5f64343a958f", :step/name ""}
;;     {:foo/id #uuid "99499dcd-a897-4d72-9dc8-7373c8a8cc72", :step/name "7"}
;;     {:foo/id #uuid "2e67832e-cf88-4694-a8b5-6d5cc959db47", :step/name ""}
;;     {:foo/id #uuid "9ed52990-7c37-4c2b-8f6e-8814d01aa8cd", :step/name ""}
;;     {:foo/id #uuid "3fa971da-ba6a-4d71-b030-5bb84ec7a1fe", :step/name "L"}
;;     {:foo/id #uuid "e9e422b6-9585-4cf9-afb5-fff8c0944e16", :step/name "G"}
;;     {:foo/id #uuid "745d5557-9218-4286-a151-7f5eaf3ef335", :step/name ""}
;;     {:foo/id #uuid "1a037059-865a-4ee1-93cc-47798b116141", :user/name "7"})
```

## License

Copyright &copy; 2020 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.txt.
