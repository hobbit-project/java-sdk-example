# HOBBIT Java SDK Example pack

This repository contains basic implementations required to start development of the HOBBIT-related software (benchmarks and systems)

# Before you start
1) Make sure that docker is installed (or install it by `sudo curl -sSL https://get.docker.com/ | sh`)
2) Clone the repository (`git clone https://github.com/hobbit-project/java-sdk-example.git`)

# How to create a benchmark
1) Configure the contents of the [ExampleDockersBuilder](https://github.com/hobbit-project/java-sdk-example/blob/master/src/main/java/org/hobbit/sdk/examples/docker/ExampleDockersBuilder.java) file (repo path, imagenamePrefix, jarFileName, dockerWorkDir)
2) Run checkHealth() from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkTest.java) (execute components as java code, you may hit the breakpoints inside your code)
3) Do 'mvn package -DskipTests=true'
4) Run buildImages() from [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java). Images will be build from jar-file under the buildDirectory folder, make sure that jar-file is actual and contains your last code changes. 
5) Run checkHealth() from [ExampleBenchmarkDockerizedTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkDockerizedTest.java) (execute components as docker containers, you may see all the logs from your containers)
6) Upload your images to remote repositories (docker login ... , docker push ...)
7) Upload ttl-files, where needed (benchmark or system).

# How to create a system for existing benchmark
To be added
