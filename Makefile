test-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealth test

package-benchmark:
	mvn -DskipTests package

build-benchmark-image:
	mvn -Dtest=BenchmarkTest#buildImages test

test-dockerized-benchmark:
	make package-benchmark
	make build-benchmark-image
	mvn -Dtest=BenchmarkTest#checkHealthDockerized test