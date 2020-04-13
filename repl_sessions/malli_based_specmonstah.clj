(ns repl-sessions.malli-based-specmonstah
  (:require [lambdaisland.specmonstah.malli :as sm-malli]
            [reifyhealth.specmonstah.core :as sm]
            [arsproto.data.schema :as schema]
            [malli.core :as m]
            [malli.util :as mu]))


(defn malli->specmonstah [schemas prefix ref id-field malli-opts]
  (into {}
        (for [[type schema] schemas]
          [type {:prefix (prefix type)
                 :schema schema
                 :relations
                 (into {} (keep (fn [[attr props schema]]
                                  (when-let [ref (ref schema)]
                                    [attr [ref id-field]]))
                                (m/map-entries schema malli-opts)))}])))

(def entity-schemas )


(schema/into-schema (schema/schema uuid?) {} [:ars/address])

(binding [schema/*ref-schema* (schema/schema uuid?)]
  (-> (sm-malli/ent-db-spec-gen {:schema specmonstah-schema}  {:ars/transport [[1]]
                                                               :ars/address [[3]]}
                                (assoc (schema/malli-opts) :size 20))
      (sm/attr-map :spec-gen)
      vals
      doall))

(malli.generator/generate [:map {:datomic? true} [:address/name #:ui{:label {:de "Name"}} string?] [:address/street #:ui{:label {:de "Strasse"}} string?] [:address/country #:ui{:label {:de "Land"}} string?] [:address/zip #:ui{:label {:de "PLZ"}} string?] [:address/place #:ui{:label {:de "Ort"}} string?]])

(def schema
  {:user {:prefix :u
          :schema [:map
                   [:foo/id uuid?]
                   [:user/name string?]]
          #_#_:constraints {:procflow.user/identity #{:uniq}}}
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




(defn ref? [schema]
  (if (vector? schema)
    (or (= :procflow/ref (first schema))
        (and (= :vector (first schema))
             (= :procflow/ref (first (second schema)))))
    false))

(defn coll-schema? [schema]
  (if (vector? schema)
    (= :vector (first schema))
    false))

(defn flatten-schema [entities]
  (for [[ent s] entities
        [attr val] (next s)]
    [ent attr val]))

(defn reference [schema]
  (if (coll-schema? schema)
    (second (second schema))
    (second schema)))

(defn malli->specmonstah [entities]
  (reduce
   (fn [acc [ent attr schema]]
     (cond-> acc
       :->           (assoc-in [ent :prefix] (keyword (name ent)))
       :->           (assoc-in [ent :schema] ent)
       (ref? schema) (assoc-in [ent :relations attr] [(reference schema) :procflow/id])
       (ref? schema) (assoc-in [ent :constraints attr] (if (coll-schema? schema)
                                                         #{:coll :uniq}
                                                         #{}))))
   {}
   (flatten-schema entities)))

(malli->specmonstah {:user [:map
                            [:id uuid?]
                            [:profile [:procflow/ref :profile]]]})


(def schemas
  '{:zoo/animal [:map
                 [:age pos-int?]
                 [:name string?]
                 [:type [:ref :animal/kind]]]
    :animal/kind [:map
                  [:kind/name string?]
                  [:kind/family string?]]})

(declare registry)

(def ref-schema
  (reify m/IntoSchema
    (-into-schema [_ props [child] options]
      (vary-meta
       (m/schema child {:registry registry})
       assoc :ref child))))

(def registry (merge m/default-registry
                     schemas
                     {:ref ref-schema}))

(sm-malli/malli->specmonstah schemas
                             (comp keyword name)
                             (comp :ref meta)
                             :db/id
                             {:registry registry})
;; => {:zoo/animal
;;     {:prefix :animal,
;;      :schema [:map [:age pos-int?] [:name string?] [:type [:ref :animal/kind]]],
;;      :relations {:type [:animal/kind :db/id]}},
;;     :animal/kind
;;     {:prefix :kind,
;;      :schema [:map [:kind/name string?] [:kind/family string?]],
;;      :relations {}}}
