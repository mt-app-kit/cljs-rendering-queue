
(ns render-synchronizer.api
    (:require [render-synchronizer.content.env :as content.env]
              [render-synchronizer.content.side-effects :as content.side-effects]
              [render-synchronizer.renderer.side-effects :as renderer.side-effects]
              [render-synchronizer.renderer.state :as renderer.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Demo
;
; @--- Initializing a renderer
; (init-renderer! :my-notifications {:destroy-duration  250   ;; <- Provides time for animated removing.
;                                    :render-duration   250   ;; <- Provides time for animated rendering.
;                                    :queue-behavior    :wait ;; <- How should the renderer behave when it is at capacity.
;                                    :renderer-capacity 1})   ;; <- Allows only one content to be rendered at a time.
;
; @--- Rendering the first content
; ;; Immediatelly adds the ID of the first notification to the list of rendered contents.
; ;; The renderer stays reserved for a 250ms animated rendering time (set above).
; (request-rendering! :my-notifications :my-first-notification)
;
; ;; The list of rendered contents now contains its first content ID.
; (get-rendered-contents :my-notifications)
; =>
; [:my-first-notification]
;
; @--- Rendering the second content
; ;; The capacity of the renderer is 1 (set above). Therefore; it doesn't add the ID of the second notification to the list of rendered contents.
; ;; The queue behavior is set to ':wait' (set above). Therefore; it adds the ID of the second notification to the rendering queue.
; (request-rendering! :my-notifications :my-second-notification)
;
; ;; The list of rendered contents still contains only one (the first) content ID.
; (get-rendered-contents :my-notifications)
; =>
; [:my-first-notification]
;
; @--- Removing the first content
; ;; Immediatelly removes the ID of the first notification from the list of rendered contents.
; ;; The renderer stays reserved for a 250ms animated removing time (set above).
; ;; Adds the ID of the second notification (waited in the rendering queue) to the list of rendered contents.
; ;; The renderer stays reserved for a 250ms animated rendering time (set above).
; (request-destroying! :my-notifications :my-first-notification)
;
; ;; Removing the ID of the first notification from the list of rendered contents allowed the second notification to be rendered.
; (get-rendered-contents :my-notifications)
; =>
; [:my-second-notification]

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Queue behavior
;
; Renderers can behave three different ways when they are at capacity and a rendering request is initiated.
;
; @title ignore
; The '{:queue-behavior :ignore}' setting instructs the renderer to ignore content rendering requests that are initiated when the renderer is at capacity.
;
; @title push
; The '{:queue-behavior :push}' setting instructs the renderer to always remove the first content in order to free up capacity for the new content.
;
; @title wait
; The '{:queue-behavior :wait}' setting instructs the renderer to place the new content onto a queue and only render it when removing former contents has freed up capacity.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Rendering flowchart
;
; - The content is already rendered  ----------------> See below.
;   - The renderer is working -----------------------> Waiting for renderer, then repeat.
;   - The renderer is available ---------------------> See below.
;     - Rerendering the same content is enabled -----> Rerendering content.
;     - Rerendering the same content is not enabled -> No-op.
; - The content is not rendered ---------------------> See below.
;   - The renderer is working -----------------------> Waiting for renderer, then repeat.
;   - The renderer is available ---------------------> See below.
;     - The renderer is not at capacity -------------> Rendering content.
;     - The renderer is at capacity (maximum number of contents are rendered)
;       - Pushed rendering is enabled ---------------> Destroying the first content, then rendering the new content.
;       - Queued rendering is enabled ---------------> Waiting for capacity, then repeat.
;       - Ignore rendering is enabled ---------------> No-op.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Destroying flowchart
;
; - The content is rendered -----> See below.
;   - The renderer is working ---> Waiting for renderer, then repeat.
;   - The renderer is available -> Destroying content.
; - The content is not rendered -> No-op.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (render-synchronizer.content.env/*)
(def get-rendered-contents content.env/get-rendered-contents)
(def get-first-content     content.env/get-first-content)
(def content-rendered?     content.env/content-rendered?)
(def content-not-rendered? content.env/content-not-rendered?)

; @redirect (render-synchronizer.content.side-effects/*)
(def request-destroying! content.side-effects/request-destroying!)
(def request-rendering!  content.side-effects/request-rendering!)

; @redirect (render-synchronizer.renderer.side-effects/*)
(def init-renderer!     renderer.side-effects/init-renderer!)
(def update-renderer!   renderer.side-effects/update-renderer!)
(def destruct-renderer! renderer.side-effects/destruct-renderer!)

; @redirect (render-synchronizer.renderer.state/*)
(def RENDERERS renderer.state/RENDERERS)
