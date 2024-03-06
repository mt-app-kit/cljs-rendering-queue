
(ns render-synchronizer.task.env
    (:require [fruits.vector.api                :as vector]
              [render-synchronizer.renderer.env :as renderer.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-not-inited?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the renderer is not initialized yet.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (renderer-not-inited? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id _]
  (let [renderer-state (renderer.env/get-renderer-state renderer-id)]
       (nil? renderer-state)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-reserved?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the renderer is currently reserved for another task.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (renderer-reserved? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id task-id]
  (let [reserved-for (renderer.env/get-renderer-state renderer-id :reserved-for)]
       (and (-> reserved-for (not= nil))
            (-> reserved-for (not= task-id)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-destroy-duration
  ; @ignore
  ;
  ; @description
  ; Returns how long does it take to destroy a specific amount of content.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ; @param (integer) content-count
  ; Default: 1
  ;
  ; @usage
  ; (get-destroy-duration :my-renderer :xxxx 1)
  ; =>
  ; 250
  ;
  ; @return (ms)
  ([renderer-id task-id]
   (get-destroy-duration renderer-id task-id 1))

  ([renderer-id _ content-count]
   (let [destroy-duration (renderer.env/get-renderer-state renderer-id :destroy-duration)]
        (* destroy-duration content-count))))

(defn get-render-duration
  ; @ignore
  ;
  ; @description
  ; Returns how long does it take to render a specific amount of content.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ; @param (integer) content-count
  ; Default: 1
  ;
  ; @usage
  ; (get-render-duration :my-renderer :xxxx 1)
  ; =>
  ; 250
  ;
  ; @return (ms)
  ([renderer-id task-id]
   (get-render-duration renderer-id task-id 1))

  ([renderer-id _ content-count]
   (let [render-duration (renderer.env/get-renderer-state renderer-id :render-duration)]
        (* render-duration content-count))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn any-task-to-do?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if there is at least one task on the queue.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (any-task-to-do? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id _]
  (if-let [task-queue (renderer.env/get-renderer-state renderer-id :task-queue)]
          (-> task-queue empty? not)))

(defn get-next-task
  ; @ignore
  ;
  ; @description
  ; Returns the first task from the task queue.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (get-next-task :my-renderer :xxxx)
  ; =>
  ; [:my-renderer :my-content :xxxx my-task-f]
  ;
  ; @return (vector)
  ; [(keyword) renderer-id
  ;  (keyword) content-id
  ;  (keyword) task-id
  ;  (function) task-f]
  [renderer-id _]
  (if-let [task-queue (renderer.env/get-renderer-state renderer-id :task-queue)]
          (first task-queue)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn pushed-rendering-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the queue behavior of the renderer is ':push'.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (pushed-rendering-enabled? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id _]
  (let [queue-behavior (renderer.env/get-renderer-state renderer-id :queue-behavior)]
       (= queue-behavior :push)))

(defn queued-rendering-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the queue behavior of the renderer is ':wait'.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (queued-rendering-enabled? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id _]
  (let [queue-behavior (renderer.env/get-renderer-state renderer-id :queue-behavior)]
       (= queue-behavior :wait)))

(defn rerender-same-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if rerendering the same content by the renderer is enabled.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (rerender-same-enabled? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id _]
  (renderer.env/get-renderer-state renderer-id :rerender-same?))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-at-capacity?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the maximum number of content of the renderer is reached.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (renderer-at-capacity? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id _]
  (let [renderer-capacity (renderer.env/get-renderer-state renderer-id :renderer-capacity)
        rendered-contents (renderer.env/get-renderer-state renderer-id :rendered-contents)]
       (vector/item-count? rendered-contents renderer-capacity)))

(defn renderer-not-at-capacity?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the maximum number of content of the renderer is not reached.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (renderer-not-at-capacity? :my-renderer :xxxx)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id task-id]
  (let [renderer-at-capacity? (renderer-at-capacity? renderer-id task-id)]
       (not renderer-at-capacity?)))
