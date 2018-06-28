# Facility - Ontology-based Component of Manufacturing Notebooks

Facility is a demonstration web server to present ontology-based information about various manufacturing
production facility resources and processes to an end user. It is intended to be used in conjunction with
SPARQL queries and Jupyter notebooks supporting manufacturing processes and operations.
The OWL ontology provided in this github repostiory is for demonstration purposes only.

## Motivation and Approach

As described in [1], mathematical models of production processes and operations can  be
verified more easily when they reference dimensionality and provenance information of data used in the analysis.
[Jupyter notebooks](http://jupyter.org) can be integrated with an organization's knowledgebase of
production properties and historical operational data to support this verification. The Facility
software supports this usage of Jupyter notebooks with an HTTP server of ontology-based information
that serves pages about the property dimenstionality and provenance based on an OWL ontology of
such information. 



## Installation

1. Download the [leiningen shell script](http://leiningen.org/).
2. Type 'lein deps' at a shell prompt in the facility directory and wait while it downloads 
    all the related libraries. 
3. Type 'lein repl' at a shell prompt in the facility directory and waiting for a clojure prompt.
4. At the clojure prompt, type (-main :port 3034) and verify that things are working: (Point browser at http://localhost:3034)

If you do not plan to do development on the server, then the easiest way to use the server is to create an uberjar: at a shell prompt in the project directory type:

```
lein uberjar
```

## Usage (if using an uberjar)


    cd 
    $ java -jar facility-standalone.jar :port 3034


## Disclaimer
The use of any software or hardware by the project does not imply a recommendation or endorsement by NIST.

The use of the project results in other software or hardware products does not imply a recommendation or endorsement by NIST of those products.

We would appreciate acknowledgement if any of the project results are used, however, the use of the NIST logo is not allowed.

NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

NIST-developed software is expressly provided “AS IS.” NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

## Credits

Peter Denno 

April Nellis 

Ibrahim Asouroko


## References

[1]: [Denno, P.; Kim, D. B., "Integrating views of properties in models of unit manufacturing processes"," International Journal of Computer Integrated Manufacturing Vol, 20, Issue 9, 2016."](https://www.tandfonline.com/doi/full/10.1080/0951192X.2015.1130259?scroll=top&needAccess=true)

# Contact Us

<a target="_blank" href="mailto:peter.denno@nist.gov">Peter Denno (peter.denno ( at ) nist.gov</a>








