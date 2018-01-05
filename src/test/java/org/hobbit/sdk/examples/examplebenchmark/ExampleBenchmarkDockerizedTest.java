package org.hobbit.sdk.examples.examplebenchmark;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.LocalEvalStorage;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.examples.examplebenchmark.docker.ExampleDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.examplebenchmark.docker.ExampleDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkDockerizedTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;

    Component benchmarkController;
    Component dataGen;
    Component taskGen;
    Component evalStorage;
    Component evalModule;
    Component systemAdapter;

    public void init(Boolean useCachedImage) throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

        benchmarkBuilder = new BenchmarkDockerBuilder(new ExampleDockersBuilder(BenchmarkController.class, BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImage));
        dataGeneratorBuilder = new DataGenDockerBuilder(new ExampleDockersBuilder(DataGenerator.class, DATAGEN_IMAGE_NAME).useCachedImage(useCachedImage));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new ExampleDockersBuilder(TaskGenerator.class, TASKGEN_IMAGE_NAME).useCachedImage(useCachedImage));

        evalStorageBuilder = new EvalStorageDockerBuilder(new ExampleDockersBuilder(EvalStorage.class, EVAL_STORAGE_IMAGE_NAME).useCachedImage(useCachedImage));

        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, SYSTEM_IMAGE_NAME).useCachedImage(useCachedImage));
        evalModuleBuilder = new EvalModuleDockerBuilder(new ExampleDockersBuilder(EvalModule.class, EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImage));

        benchmarkController = benchmarkBuilder.build();
        dataGen = dataGeneratorBuilder.build();
        taskGen = taskGeneratorBuilder.build();
        evalStorage = evalStorageBuilder.build();
        evalModule = evalModuleBuilder.build();
        systemAdapter = systemAdapterBuilder.build();
    }

    @Test
    @Ignore
    public void buildImages() throws Exception {

        init(false);

        ((AbstractDockerizer)benchmarkController).prepareImage();
        ((AbstractDockerizer)dataGen).prepareImage();
        ((AbstractDockerizer)taskGen).prepareImage();
        ((AbstractDockerizer)evalStorage).prepareImage();
        ((AbstractDockerizer)systemAdapter).prepareImage();
        ((AbstractDockerizer)evalModule).prepareImage();
    }

    @Test
    public void checkHealth() throws Exception {

        Boolean useCachedImages = true;

        init(useCachedImages);

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer.run();


        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName())
                        .taskGenerator(taskGen).taskGeneratorImageName(taskGeneratorBuilder.getImageName())
                        .evalStorage(evalStorage).evalStorageImageName(evalStorageBuilder.getImageName())
                        .evalModule(evalModule).evalModuleImageName(evalModuleBuilder.getImageName())
                        .systemContainerId(systemAdapterBuilder.getImageName())
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();


        //you can run clear java-code instead of dockerized one

        //benchmarkController = new DummyBenchmarkController();
        //systemAdapter = new DummySystemAdapter();


        componentsExecutor.submit(benchmarkController);
        componentsExecutor.submit(systemAdapter, systemAdapterBuilder.getImageName());

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }






}
