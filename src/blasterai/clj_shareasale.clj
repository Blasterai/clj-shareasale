(ns blasterai.clj-shareasale
  (:require [clj-http.client :as client]
            [tick.core as t]))

(def merchat-id 89352)
(def token "UFy4yd9g1NGsmv3t")
(def api-secret "SEk6vy8b8VOqox4iUFy4yd9g1NGsmv3t")

(defn client [{:keys [merchant-id token secret]}]
  )

(comment
  (client/get "http://example.com")
  (client/post "http://example.com")
  (foo))



