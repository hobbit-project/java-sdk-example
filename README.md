# HOBBIT Java SDK Example pack

This repository contains starting kit for [HOBBIT components](https://github.com/hobbit-project/platform/wiki/Develop-a-benchmark-component-in-Java) development using the proposed [HOBBIT Java SDK](https://github.com/hobbit-project/java-sdk).

The repository may be cloned and used as basis for every new project (benchmark or system) on the HOBBIT Platform.

The detailed description of the development and debug process with Java SDK can be found [here](https://github.com/hobbit-project/java-sdk).

# Usage
## Before you start
1) Make sure that docker (v17 and later) is installed (or install it by `sudo curl -sSL https://get.docker.com/ | sh`)
2) Make sure that maven (v3 and later) is installed (or install it by `sudo apt-get install maven`)
3) Clone this repository (`git clone https://github.com/hobbit-project/java-sdk-example.git`)
4) Make sure that hobbit-java-sdk dependency (declared in [pom.xml](https://github.com/hobbit-project/java-sdk-example/blob/master/pom.xml)) is installed into your local maven repository (or install it by `mvn validate`)

## How to create a benchmark
1) Configure the contents of the [ExampleDockersBuilder](https://github.com/hobbit-project/java-sdk-example/blob/master/src/main/java/org/hobbit/sdk/examples/examplebenchmark/docker/ExampleDockersBuilder.java) file (repo path, imagenamePrefix, jarFileName, dockerWorkDir). This configuration will be used for dynamic creation of DockerFiles for all components in your project. 
If you want to use specific DockerFiles for some of your components, when specify them as dockerFileReaders by calling `.dockerFileReader(your readers)` for ExampleDockersBuilder for particular components in  [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java).
2) Run the `checkHealth()` method from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkTest.java)). This will execute the components as pure java codes and you may hit the breakpoints inside any of them.
3) Pack source codes with dependencies into jar file (`mvn package -DskipTests=true`)
4) Run the `buildImages()` method from [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java). Images will be build from jar-file you have packed on step 4, so make sure that jar-file is actual and contains your last changes. 
5) Execute components as docker containers (run the `checkHealth()` method from [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java)). You may see all the logs from your containers.
6) Upload your images to remote repositories (by manual running the `docker login ... `, `docker push ...` commands)
7) Upload ttl-files, where needed (benchmark or system).

## How to create a system for existing benchmark
If benchmark designer provides URLs of docker images for all the components of the benchmark, then you can run that images locally and debug your system under the particular bechmark.
1) Specify image names for pull-based dockerizers (see the [ExampleSystemTest.java](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleSystemTest.java)). If benchmark does not contain the full amount of components, when remove the omited components from MultipleCommandsReaction.
2) Run the `checkHealth()` method from [ExampleSystemTest.java](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleSystemTest.java). You may swich between dockerized and pure java implemetation by commenting/uncommenting the line `systemAdapter = new SystemAdapter();`

If you haven't URLs of benchmark images, but have source-codes of the benchmark then you can run them locally (non-dockerized mode will be enought). See the `How to create a benchmark` section.

## FAQ
If `checkHealth()` methods hangs up, then look for the errors (via Ctrl+F) in console output. Components execution is parallel, so critical errors will to fail the whole process and requires developers to control output.
