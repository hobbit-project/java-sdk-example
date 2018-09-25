test-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealth test

package:
	mvn -DskipTests package

build-images:
	make package
	mvn -Dtest=BenchmarkTest#buildImages test

test-dockerized:
	make package
	make build-images
	mvn -Dtest=BenchmarkTest#checkHealthDockerized test