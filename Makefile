test-benchmark:
	mvn -Dtest=ExampleBenchmarkTest#checkHealth test

package-benchmark:
	mvn -DskipTests package

build-benchmark-image:
	mvn -Dtest=ExampleBenchmarkTest#buildImages test

test-dockerized-benchmark:
	mvn -Dtest=ExampleBenchmarkTest#checkHealthDockerized test
