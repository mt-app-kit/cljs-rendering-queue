
(ns render-synchronizer.content.side-effects
    (:require [fruits.random.api                     :as random]
              [render-synchronizer.task.side-effects :as task.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn request-destroying!
  ; @description
  ; Initiates removing the ID of a specific content from the list of rendered contents of the rendered.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (request-destroying! :my-renderer :my-content)
  [renderer-id content-id]
  (let [task-id (random/generate-keyword)]
       (task.side-effects/select-destroying-method! renderer-id content-id task-id)))

(defn request-rendering!
  ; @description
  ; Initiates adding the ID of a specific content to the list of rendered contents of the rendered.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (request-rendering! :my-renderer :my-content)
  [renderer-id content-id]
  (let [task-id (random/generate-keyword)]
       (task.side-effects/select-rendering-method! renderer-id content-id task-id)))
