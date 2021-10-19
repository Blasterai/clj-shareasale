(ns clj-shareasale.client
  (:require [tick.core :as t]
            [clj-http.client :as client]
            [clojure.string :as str]))

(def url "https://shareasale.com/w.cfm")

(defn title-case 
  "Converts any strING to String"
  [s]
  (let [f (first s)
        r (str/join (rest s))]
    (-> [(str/upper-case f) (str/lower-case r)]
        (str/join))))

(defn instant-breakdown
  "Takes an instant of time and breaks it down into units."
  [t]
  {:day  (t/day-of-week t)
   :month  (t/month t)
   :b (->> (t/month t) str title-case (take 3) (str/join))
   :dd (t/day-of-month t)
   :a (->> (t/day-of-week t) str title-case (take 3) (str/join))
   :MM (t/int (t/month t))
   :yyyy (t/int (t/year t))
   :mm (t/minute t)
   :HH (t/hour t)
   :ss (t/second t)})

(defn timestamp []
  (let [now (instant-breakdown (t/in (t/now) "UTC"))]
    (-> [(:a now) ", " (:dd now) " " (:b now) " " (:yyyy now) " " (:HH now) ":" (:MM now) ":" (:ss now) " +0000"]
        (str/join))))

;; timestamp format: 'Tue, 19 Oct 2021 16:22:14 +0000'
;; 'merchantId=89352&token=UFy4yd9g1NGsmv3t&version=3.0&action=bannerList'
(defn request-params
  "Genreate request params string"
  [{:keys [merchant-id api-token api-version action-verb]}]
  {:pre [(some? merchant-id) (some? api-token) (some? action-verb)]}
  (let [api-version (or api-version "3.0")]
    (-> ["merchantId=" merchant-id
         "&token=" api-token
         "&version=" api-version
         "&action=" action-verb]
        (str/join)))
  )

(defn hexify [digest]
  (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))

(defn sha256digest [b]
  (.digest (java.security.MessageDigest/getInstance "SHA-256") b))

(defn signature
  [{:keys [api-token ts action-verb api-secret]}]
  {:pre [(some? api-token) (some? action-verb) (some? api-secret)]}
  (let [tstamp (or ts (timestamp))
        sig (->> [api-token tstamp action-verb api-secret]
                 (str/join ":"))]
    (-> (.getBytes sig "UTF-8")
        sha256digest
        hexify))
  )

(defn request-data [{:keys [api-token ts action-verb api-secret] :as data}]
  (let [tstamp (or ts (timestamp))
        new-data (assoc data :ts tstamp)]
    {:params (request-params new-data)
     :headers
     {:x-ShareASale-Date (or ts (timestamp))
      :x-ShareASale-Authentication (signature new-data)}}))

(defn request [credentials action-verb]
  (let [data (request-data (assoc credentials :action-verb action-verb))]
    (-> (client/get (str url "?" (:params data)) {:headers (:headers data)})
        :body)))



