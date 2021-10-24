(ns lambdaisland.specmonstah.malli-test
  (:require [clojure.test :as t :refer [deftest is]]
            [lambdaisland.specmonstah.malli :as sm-malli]
            [matcher-combinators.test]
            [reifyhealth.specmonstah.core :as sm]))

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

(deftest basic-value-gen
  (is (match?
       [{:foo/id uuid?
         :procedure/id uuid?
         :procflow.procedure/steps [uuid?]
         :procflow.procedure/owner uuid?}
        {:foo/id uuid? :step/name string?}
        {:foo/id uuid? :user/name string?}]
       (-> (sm-malli/ent-db-spec-gen
            {:schema schema}
            {:procedure [[1]]
             :step [[1]]})
           (sm/attr-map :spec-gen)
           vals))))
