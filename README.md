# facility

This is early development of a web facility that would present various factory resources
to an end user. We (talking to the Vanderbilt folks) could potentially use this 
for elaborate queries, doing the query development in Clojure. The alternative is to 
do those queries directly from JENA. 

## Installation

I've set this up to run as a jar (uberjar). See Usage. 

If you want to do real development with it, that would involve minimally:

1. Download the [leiningen shell script](http://leiningen.org/).
2. Type 'lein deps' at a shell prompt in the facility directory and waiting while it downloads 
    all the related libraries. 
3. Type 'lein repl' at a shell prompt in the facility directory and waiting for a clojure prompt.
4. At the clojure prompt, type (-main :port 3034)

One can also build a an uberjar by typing 'lein uberjar' at a shell prompt in the project directory.

## Usage (if using an uberjar)


    cd 
    $ java -jar facility-standalone.jar :port 3034


## Options

FIXME: listing of options this app accepts.

## Examples


### Bugs

Doesn't do much. 


## Disclaimer
The use of any software or hardware by the project does not imply a recommendation or endorsement by NIST.

The use of the project results in other software or hardware products does not imply a recommendation or endorsement by NIST of those products.

We would appreciate acknowledgement if any of the project results are used, however, the use of the NIST logo is not allowed.

NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

NIST-developed software is expressly provided “AS IS.” NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.




Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
