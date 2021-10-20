(ns clj-shareasale.client
  (:require [tick.core :as t]
            [clj-http.client :as client]
            [clojure.string :as str]))

(def ^:private url "https://shareasale.com/w.cfm")

(defn- title-case 
  "Converts any strING to String"
  [s]
  (let [f (first s)
        r (str/join (rest s))]
    (-> [(str/upper-case f) (str/lower-case r)]
        (str/join))))

(defn- instant-breakdown
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

(defn- timestamp []
  (let [now (instant-breakdown (t/in (t/now) "UTC"))]
    (-> [(:a now) ", " (:dd now) " " (:b now) " " (:yyyy now) " " (:HH now) ":" (:MM now) ":" (:ss now) " +0000"]
        (str/join))))

(defn- hexify [digest]
  (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))

(defn- sha256digest [b]
  (.digest (java.security.MessageDigest/getInstance "SHA-256") b))

(defn- sign
  [{:keys [api-token ts action-verb api-secret]}]
  {:pre [(some? api-token) (some? action-verb) (some? api-secret)]}
  (let [tstamp (or ts (timestamp))
        sig (->> [api-token tstamp action-verb api-secret]
                 (str/join ":"))]
    (-> (.getBytes sig "UTF-8")
        sha256digest
        hexify)))

(defn- prepare-headers [{:keys [api-token ts action-verb api-secret api-version] :as data}]
  (let [tstamp (or ts (timestamp))
        api-version (or api-version "3.0")
        new-data (assoc data :ts tstamp :api-version api-version)]
    {:x-ShareASale-Date tstamp
     :x-ShareASale-Authentication (sign new-data)}))

(defn- prepare-params
  [{:keys [merchant-id api-token api-secret action-verb] :as credentials} & query-params]
  (-> {:merchantId merchant-id
       :token api-token
       :version (or (:api-version query-params) "3.0")
       :action action-verb}
      (merge (or (first query-params) {}))
      (dissoc :api-version :action-verb)))

(defn- request! [credentials action-verb &{:keys [] :as params}]
  (let [verb (name action-verb)]
    (let [new-params (prepare-params (assoc credentials :action-verb verb) params)]
      (-> (client/get url {:query-params new-params
                           :headers (prepare-headers (assoc credentials :action-verb verb))})
          :body))))

(defn make-client
  "Makes a client function used to execute action verbs. Resulting function can be run like this:

  (client :bannerList)
  (client :transactiondetail :format \"csv\" :datestart \"10/15/2021\")

  First argument is the action word for Shareasale API.
  The rest are optional query params. "
  [{:keys [api-token api-secret] :as credentials}]
  {:pre [(some? api-token) (some? api-secret)]}
  (partial request! credentials))

;; Example
(comment
  (def credentials {:api-token "token"
                    :api-secret "secret"
                    :merchant-id 12345})
  (def client (make-client credentials))
  (client :transactiondetail :format "csv" :datestart "10/01/2021")
  (->> (client :transactiondetail :format "csv" :datestart "10/01/2021")
       (spit "response.csv")))


