# facility

This is early development of a web facility that would present various factory resources
to an end user. We (talking to the Vanderbilt folks) could potentially use this 
for elaborate queries, doing the query development in Clojure. The alternative is to 
do those queries directly from JENA. 

## Installation

I've set this up to run as a jar (uberjar). See Usage. 

If you want to do real development with it, that would involve minimally:

(1) Download the leiningen shell script.
(2) Type 'lein deps' at a shell prompt in the facility directory and waiting while it downloads 
    all the related libraries. 
(3) Commen out the [gov.nist.pod] line in facility.clj (This is an exercise left for the reader.)
(4) Type 'lein repl' at a shell prompt in the facility directory and waiting for a clojure prompt.
(5) At the clojure prompt, type (-main :port 3034)

More realistically, doing development would require and IDE (emacs + CIDER). Drop me a note
if you are interested. 

## Usage


    cd 
    $ java -jar facility-standalone.jar :port 3034


## Options

FIXME: listing of options this app accepts.

## Examples


### Bugs

Doesn't do much. 

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2015 FIXME



Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
