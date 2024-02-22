
# cljs-rendering-queue

### Overview

The <strong>cljs-rendering-queue</strong> is a simple rendering queue handler for Clojure projects.

### Description

This library is designed for maintaining a rendering logic for content renderers (e.g., modals, notifications, ...),
such as limiting the number of rendered contents, allowing only one content to render at a time (per renderer), etc.

> Functions of this library don't have side effects in addition to maintaining a list of identifiers of rendered contents.

### deps.edn

```
{:deps {mt-app-kit/cljs-rendering-queue {:git/url "https://github.com/mt-app-kit/cljs-rendering-queue"
                                         :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/mt-app-kit/cljs-rendering-queue/tree/release).

### Documentation

The <strong>cljs-rendering-queue</strong> functional documentation is [available here](https://mt-app-kit.github.io/cljs-rendering-queue).

### Changelog

You can track the changes of the <strong>cljs-rendering-queue</strong> library [here](CHANGES.md).
