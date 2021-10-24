(ns dependencies
  (:require [lambdaisland.classpath :as licp]))

(licp/update-classpath! {:aliases [:test]})
