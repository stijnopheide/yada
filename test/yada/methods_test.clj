;; Copyright © 2015, JUXT LTD.

(ns yada.methods-test
  (:require
   [byte-streams :as bs]
   [clojure.test :refer :all]
   [juxt.iota :refer (given)]
   [ring.mock.request :as mock]
   [ring.util.codec :as codec]
   [schema.core :as s]
   [yada.methods :refer (Get Post)]
   [yada.resources.misc :refer (just-methods)]
   [yada.protocols :as p]
   [yada.yada :as yada :refer [yada]]))

(deftest post-test
  (let [handler (yada
                 (just-methods
                  :post {:response (fn [ctx]
                                     (assoc (:response ctx)
                                            :status 201
                                            :body "foo"))}))]
    (given @(handler (mock/request :post "/"))
      :status := 201
      [:body bs/to-string] := "foo")))

(deftest dynamic-post-test
  (let [handler (yada
                 (just-methods
                  :post {:response (fn [ctx]
                                     (assoc (:response ctx)
                                            :status 201 :body "foo"))}))]
    (given @(handler (mock/request :post "/"))
      :status := 201
      [:body bs/to-string] := "foo")))

(deftest multiple-headers-test
  (let [handler
        (yada
         (just-methods
          :post {:response (fn [ctx] (assoc (:response ctx)
                                           :status 201 :headers {"set-cookie" ["a" "b"]}))}))]
    (given @(handler (mock/request :post "/"))
      :status := 201
      [:headers "set-cookie"] := ["a" "b"])))

;; Allowed methods ---------------------------------------------------

;; To ensure coercion to StringResource which satisfies GET (tested
;; below)
(require 'yada.resources.string-resource)

(deftest allowed-methods-test
  (testing "methods-deduced"
    (are [r e] (= (:allowed-methods (yada r)) e)
      nil #{:get :head :options}
      "Hello" #{:get :head :options}
      (reify Get (GET [_ _] "foo")) #{:get :head :options}
      (reify
        Get (GET [_ _] "foo")
        Post (POST [_ _] "bar")) #{:get :post :head :options})))
