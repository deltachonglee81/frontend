(ns frontend.notifs
  (:require [frontend.utils :as utils :include-macros true]
            [frontend.localstorage :as localstorage]))


;; Gotta edit this to only happen when a user first toggles that switch
;; This should actually be a little function called n/notify  that simply constructs a notification out of optional keyword arguments! (should have multiple signatures so if you just pass 1 arg it become the title)
(defn request-permissions [promise-fn]
  (-> js/Notification
      (.requestPermission)
      (.then promise-fn)))

;; Some notes about properties
;; The title should be 32 characters MAX, note that if using system default, only 22 chars are visible on hover (because of resulting UI), so keep critical information 22 chars
;; The body property should be a MAX of 42 characters (this is assuming a default text size with San Francisco, the default system font on macOS)
(defn notify [title properties]
  (def new-notif (new js/Notification
                      title
                      (clj->js (merge
                                 properties
                                 {:lang "en"})))))

(defn ask-then-notify [title properties]
  (request-permissions
    (fn [status]
      (notify title properties))))

(defn email-path [text]
  (utils/cdn-path (str "/img/email/"text"/icon@3x.png")))

(def failed-email-img (email-path "failed"))
(def passed-email-img (email-path "passed"))

(defn notify-build-done [build project build-num]
  (let [status (:status build)
        properties (case status
                     "no_tests" {:icon passed-email-img :body "Looks like there were no tests to run."}
                     "success" {:icon passed-email-img :body "Yay, your tests passed!"}
                     "fixed" {:icon passed-email-img :body "Yay, all your tests are fixed!"}
                     "failed" {:icon failed-email-img :body "Looks like some tests failed."}
                     "infrastructure_fail" {:icon failed-email-img :body "Darn, something went wrong."}
                     {:icon failed-email-img :body "Whoops, no status inforamtion."})]
    (notify (str project " #" build-num) properties)))
