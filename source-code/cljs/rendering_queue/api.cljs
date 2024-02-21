
(ns rendering-queue.api
    (:require [rendering-queue.env :as env]
              [rendering-queue.state :as state]
              [rendering-queue.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Rendering flowchart
;
; - The content is already rendered
;   - The renderer is working -----------------------> Waiting for renderer, then repeat.
;   - The renderer is available ---------------------> See below.
;     - Rerendering the same content is enabled -----> Rerendering content.
;     - Rerendering the same content is not enabled -> No-op.
; - The content is not rendered ---------------------> See below.
;   - The renderer is working -----------------------> Waiting for renderer, then repeat.
;   - The renderer is available ---------------------> See below.
;     - The renderer has capacity -------------------> Rendering content.
;     - The renderer is at capacity (maximum number of contents are rendered)
;       - Pushed rendering is enabled ---------------> Destroying the first content, then rendering the new content.
;       - Queued rendering is enabled ---------------> Waiting for capacity, then repeat.
;       - Ignore rendering is enabled ---------------> No-op.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Destroying flowchart
;
; - The content is rendered
;   - The renderer is working ---> Waiting for renderer, then repeat.
;   - The renderer is available -> Destroying content.
; - The content is not rendered -> No-op.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (rendering-queue.env/*)
(def renderer-at-capacity?     env/renderer-at-capacity?)
(def renderer-not-at-capacity? env/renderer-not-at-capacity?)
(def content-rendered?         env/content-rendered?)
(def content-not-rendered?     env/content-not-rendered?)

; @redirect (rendering-queue.side-effects/*)
(def init-renderer!      side-effects/init-renderer!)
(def update-renderer!    side-effects/update-renderer!)
(def destruct-renderer!  side-effects/destruct-renderer!)
(def request-destroying! side-effects/request-destroying!)
(def request-rendering!  side-effects/request-rendering!)

; @redirect (rendering-queue.state/*)
(def RENDERERS state/RENDERERS)
