
(ns rendering-queue.prototypes)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-props-prototype
  ; @ignore
  ;
  ; @param (keyword) renderer-id
  ; @param (map) renderer-props
  ;
  ; @return (map)
  [_ renderer-props]
  (merge {:destroy-duration  0
          :render-duration   0
          :queue-behavior    :ignore
          :renderer-capacity 1
          :rerender-same?    false}
         (-> renderer-props)))
