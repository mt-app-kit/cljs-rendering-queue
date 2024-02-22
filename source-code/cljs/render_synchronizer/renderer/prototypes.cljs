
(ns render-synchronizer.renderer.prototypes)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-props-prototype
  ; @ignore
  ;
  ; @param (keyword) renderer-id
  ; @param (map) renderer-props
  ;
  ; @return (map)
  ; {:destroy-duration (ms)
  ;  :queue-behavior (keyword)
  ;  :renderer-capacity (integer)
  ;  :render-duration (ms)
  ;  :rerender-same? (boolean)
  ;  ...}
  [_ renderer-props]
  (merge {:destroy-duration  0
          :render-duration   0
          :queue-behavior    :ignore
          :renderer-capacity 1
          :rerender-same?    false}
         (-> renderer-props)))
