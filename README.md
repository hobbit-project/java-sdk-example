# HOBBIT Java SDK Example pack

This repository contains starting kit for benchmark/system development using the proposed [HOBBIT Java SDK](https://github.com/hobbit-project/java-sdk).

The repository may be cloned and used as basis for every new project on the HOBBIT Platform.

The detailed description of the development and debug process with Java SDK can be found [here](https://github.com/hobbit-project/java-sdk).

# Usage
## Before you start
1) Make sure that docker is installed (or install it by `sudo curl -sSL https://get.docker.com/ | sh`)
2) Clone this repository (`git clone https://github.com/hobbit-project/java-sdk-example.git`)
3) Make sure that hobbit-java-sdk dependency is installed into your local maven repository (or install it by `mvn validate`)

## How to create a benchmark
1) Configure the contents of the [ExampleDockersBuilder](https://github.com/hobbit-project/java-sdk-example/blob/master/src/main/java/org/hobbit/sdk/examples/docker/ExampleDockersBuilder.java) file (repo path, imagenamePrefix, jarFileName, dockerWorkDir)
2) Execute components as java codes (run checkHealth() from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkTest.java)) you may hit the breakpoints inside your code.
3) Pack source codes with dependencies into jar file ('mvn package -DskipTests=true')
4) Run buildImages() from [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java). Images will be build from jar-file you have packed on step 4, so make sure that jar-file is actual and contains your last changes. 
5) Execute components as docker containers (run checkHealth() from [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java)). You may see all the logs from your containers.
6) Upload your images to remote repositories (docker login ... , docker push ...)
7) Upload ttl-files, where needed (benchmark or system).

## How to create a system for existing benchmark
To be added
