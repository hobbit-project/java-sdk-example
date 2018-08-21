package org.hobbit.sdk.examples.examplebenchmark;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.examples.examplebenchmark.system.SystemAdapter;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.hobbit.sdk.utils.commandreactions.CommandReactionsBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.core.Constants.BENCHMARK_PARAMETERS_MODEL_KEY;
import static org.hobbit.core.Constants.HOBBIT_EXPERIMENT_URI_KEY;
import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;
import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.examplebenchmark.Constants.*;


/**
 * @author Pavel Smirnov
 *
 * This test shows how to debug your system under already published benchmark images
 * if docker images of benchmarkController components are available online
 *
 *
 */


public class SystemTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;




    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;


    public void init(boolean useCachedImages) throws Exception {

        benchmarkBuilder = new BenchmarkDockerBuilder(new PullBasedDockersBuilder(BENCHMARK_IMAGE_NAME));
        dataGeneratorBuilder = new DataGenDockerBuilder(new PullBasedDockersBuilder(DATAGEN_IMAGE_NAME));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new PullBasedDockersBuilder(TASKGEN_IMAGE_NAME));
        evalStorageBuilder = new EvalStorageDockerBuilder(new PullBasedDockersBuilder(EVAL_STORAGE_IMAGE_NAME));
        evalModuleBuilder = new EvalModuleDockerBuilder(new PullBasedDockersBuilder(EVALMODULE_IMAGE_NAME));

        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages));

    }

    @Test
    @Ignore
    public void buildImages() throws Exception {
        init(false);
        systemAdapterBuilder.build().prepareImage();
    }

    @Test
    public void checkHealth() throws Exception {
        checkHealth(false);
    }

    @Test
    public void checkHealthDockerized() throws Exception {
        checkHealth(true);
    }


    private void checkHealth(boolean dockerize) throws Exception {

        Boolean useCachedImages = true;

        init(useCachedImages);

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        environmentVariables.set(org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY, rabbitMqDockerizer.getHostName());
        environmentVariables.set(org.hobbit.core.Constants.HOBBIT_SESSION_ID_KEY, "session_"+String.valueOf(new Date().getTime()));


        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor();

        rabbitMqDockerizer.run();

        Component benchmarkController = benchmarkBuilder.build();
        Component dataGen = dataGeneratorBuilder.build();
        Component taskGen = taskGeneratorBuilder.build();
        Component evalStorage = evalStorageBuilder.build();
        Component evalModule = evalModuleBuilder.build();
        Component systemAdapter = new SystemAdapter();

        if(dockerize)
            systemAdapter = systemAdapterBuilder.build();


        //comment the .systemAdapter(systemAdapter) line below to use the code for running from python
        CommandReactionsBuilder commandReactionsBuilder = new CommandReactionsBuilder(componentsExecutor, commandQueueListener)
                .benchmarkController(benchmarkController).benchmarkControllerImageName(BENCHMARK_IMAGE_NAME)
                .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName())
                .taskGenerator(taskGen).taskGeneratorImageName(taskGeneratorBuilder.getImageName())
                .evalStorage(evalStorage).evalStorageImageName(evalStorageBuilder.getImageName())
                .evalModule(evalModule).evalModuleImageName(evalModuleBuilder.getImageName())
                .systemAdapter(systemAdapter).systemAdapterImageName(SYSTEM_IMAGE_NAME)
                //.customContainerImage(systemAdapter, DUMMY_SYSTEM_IMAGE_NAME)
                ;

        commandQueueListener.setCommandReactions(
                commandReactionsBuilder.buildStartCommandsReaction(), //comment this if you want to run containers on a platform instance (if the platform is running)
                commandReactionsBuilder.buildTerminateCommandsReaction(),
                commandReactionsBuilder.buildPlatformCommandsReaction()
        );


        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        // Start components without sending command to queue. Components will be executed by SDK, not the running platform (if it is running)
        String benchmarkContainerId = "benchmark";
        componentsExecutor.submit(benchmarkController, benchmarkContainerId, new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ createBenchmarkParameters() });

        String systemContainerId = "system";
        componentsExecutor.submit(systemAdapter, systemContainerId, new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+ createSystemParameters() });

        //Alternative. Start components via command queue (will be executed by the platform (if running))
//        String benchmarkContainerId = commandQueueListener.createContainer(benchmarkBuilder.getImageName(), "benchmark", new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ createBenchmarkParameters() });
//        String systemContainerId = commandQueueListener.createContainer(systemAdapterBuilder.getImageName(), "system" ,new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+ createSystemParameters() });

        environmentVariables.set("BENCHMARK_CONTAINER_ID", benchmarkContainerId);
        environmentVariables.set("SYSTEM_CONTAINER_ID", systemContainerId);


        commandQueueListener.waitForTermination();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    public String createBenchmarkParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(BENCHMARK_URI+"benchmarkParam1", 123);
        kv.setValue(BENCHMARK_URI+"benchmarkParam2", 456);
        return kv.encodeToString();
    }

    public String createSystemParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(SYSTEM_URI+"systemParam1", 123);
        //kv.setValue(SYSTEM_URI+SYSTEM_CONTAINERS_COUNT_KEY, 2);
        return kv.encodeToString();
    }

}
