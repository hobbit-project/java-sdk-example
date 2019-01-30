# HOBBIT Java SDK Example [![Build Status](https://travis-ci.org/hobbit-project/java-sdk-example.svg?branch=master)](https://travis-ci.org/hobbit-project/java-sdk-example)

This repository contains all types of [HOBBIT-compatible components](https://github.com/hobbit-project/platform/wiki/Develop-a-benchmark-component-in-Java) and number of tests, requred to debug components locally without having a running instance of the platform.  

The repository may be cloned and used as HOBBIT-compatible basis for the future project (benchmark or system). The tests allow developers to debug components either as pure java codes or being packaged into docker containers. Fully tested docker images may be uploaded and executed in the online platform without any modifications.

The added value of the Java SDK against standart HOBBIT approach decribed [here](https://github.com/hobbit-project/java-sdk-example/blob/master/SDK_vs_Standard_Way.pdf).

# Usage
## Before you start
1) Make sure that Oracle 1.8 (or higher) is installed (`java -version`). Or install it by the `sudo add-apt-repository ppa:webupd8team/java && sudo apt-get update && sudo apt-get install oracle-java8-installer -y`.
2) Make sure that docker (v17 and later) is installed (or install it by `sudo curl -sSL https://get.docker.com/ | sh`)
3) Make sure that maven (v3 and later) is installed (or install it by `sudo apt-get install maven`)
4) Add the `127.0.0.1 rabbit` line to `/etc/hosts` (Linux) or `C:\Windows\System32\drivers\etc\hosts` (Windows)
5) Clone this repository (`git clone https://github.com/hobbit-project/java-sdk-example.git`)
6) Install SDK dependency into your local maven repository (`mvn validate`)

## How to create a benchmark
1) Please find the basic benchmark component implementations in the [sources folder](https://github.com/hobbit-project/java-sdk-example/tree/master/src/main/java/org/hobbit/sdk/examples/examplebenchmark/benchmark). You may extend the components with logic of your benchmark and debug the components as pure java codes by running the `make test-benchmark` command or execute `checkHealth()` method from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleBenchmarkTest.java)) in IDE. You may specify input parameters models for benchmark and system you are running.
2) Once your components work well as pure java codes, you can test them components being packed into docker containers. In order to do that you have to modify/configure the values of constants defined in the [Constants](https://github.com/hobbit-project/java-sdk-example/blob/master/src/main/java/org/hobbit/sdk/examples/examplebenchmark/Constants.java) file (repo path, imagenamePrefix, dockerWorkDir). This configuration will be used for dynamic creation of DockerFiles for all components (benchmark controller, data generator, task generator, eval storage, eval module, etc.) of your project. If you want to use specific DockerFile for any of the components, when specify them as dockerFileReaders by calling `.dockerFileReader(yourReader)` for <ComponentName>DockersBuilder in the [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleBenchmarkTest.java). If you need to put dataset into a specific docker image - then use the `addFileOrFolder(path)` method to the particular <ComponentName>DockersBuilder builder.
3) Pack source codes with dependencies into jar file (via the `make package` command)
4) Run the `make build-images` or execute the `buildImages()` method from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleBenchmarkTest.java) to build docker images. Images will be build from jar-file you have packed on step 4, so make sure that jar-file is actual and contains your last changes. 
5) Execute components as docker containers (run the `make test-dockerized-benchmark` command or `checkHealthDockerized()` method from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleBenchmarkTest.java) via IDE). You may see all the logs from your containers.
6) Once you have working images of your project you may upload the to remote repositories (by manual running the `docker login ... `, `docker push ...` commands) without any changes.
7) Upload ttl-files, where needed (benchmark or system).

## How to create a system for existing benchmark
If benchmark designer provides URLs of docker images for all the components of the benchmark or you have such images locally, then you can run your system under particular benchmark workload. If you have source codes of the benchmark you may eigher build these images locally or use the components as pure java code - please refer the section above: 
1) Specify image names for pull-based dockerizers (see the [ExampleSystemTest.java](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleSystemTest.java)). If benchmark does not contain the full amount of components, when remove the omited components from MultipleCommandsReaction.
2) Run the `make test-benchmark` command or execute the `checkHealth()` test from [ExampleSystemTest.java](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleSystemTest.java) to debug your system as pure java code. 
3) Once your system works well as pure java code you may test it being packed into docker container (run the `make test-dockerized-benchmark` command or execute the `checkHealthDockerized()` test from [ExampleSystemTest.java](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleSystemTest.java) in IDE). To configure image settings please refer to the points 2-7 from the section above. 

## FAQ
If the benchmark execution hangs up, then search for the errors (via Ctrl+F) in console output. Components execution is parallel, so errors may be shown not in the bottom ot the output.

The detailed description of the development and debug process with Java SDK can be found [here](https://github.com/hobbit-project/java-sdk).
