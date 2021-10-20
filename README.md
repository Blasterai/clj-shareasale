# clj-shareasale

Clojure client for Shareasale API.

## Usage

Create a map with your credentials, then use it to generate a client function, which you can use to make requests.
First argument to `client` is the action-verb for Shareasale API. All keyword arguments after that are added as query
params to the request.

```clojure
(def credentials {:api-token "token"
                    :api-secret "secret"
                    :merchant-id 12345})
(def client (make-client credentials))

(client :transactiondetail :format "csv" :datestart "10/01/2021")
```


## Maintenance

Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables (requires the `ci` task be run first):

    $ clojure -T:build deploy

Your library will be deployed to io.github.blasterai/clj-shareasale on clojars.org by default.

## License

Copyright © 2021 Mb

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.
