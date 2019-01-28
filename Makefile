install-deps:
	mvn validate

test-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealth test

package:
	mvn -DskipTests -DincludeDeps=true package

build-images:
	mvn -Dtest=BenchmarkTest#buildImages surefire:test

test-dockerized-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealthDockerized test


push-images:
	sudo docker push git.project-hobbit.eu:4567/sdk-examples/sdk-example-benchmark/benchmark-controller
	sudo docker push git.project-hobbit.eu:4567/sdk-examples/sdk-example-benchmark/datagen
	sudo docker push git.project-hobbit.eu:4567/sdk-examples/sdk-example-benchmark/taskgen
	sudo docker push git.project-hobbit.eu:4567/sdk-examples/sdk-example-benchmark/eval-storage
	sudo docker push git.project-hobbit.eu:4567/sdk-examples/sdk-example-benchmark/system-adapter
	sudo docker push git.project-hobbit.eu:4567/sdk-examples/sdk-example-benchmark/eval-module