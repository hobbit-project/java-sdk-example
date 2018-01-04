package org.hobbit.sdk.examples;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.LocalEvalStorage;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.examples.docker.ExampleDockersBuilder;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.docker.ExampleDockersBuilder.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkDockerizedTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BuildBasedDockersBuilder benchmarkDB;
    BuildBasedDockersBuilder dataGeneratorDB;
    BuildBasedDockersBuilder taskGeneratorDB;
    BuildBasedDockersBuilder evalStorageDB;
    BuildBasedDockersBuilder systemAdapterDB;
    BuildBasedDockersBuilder evalModuleDB;

    public void init() throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

        benchmarkDB = new BenchmarkDockerBuilder(new ExampleDockersBuilder(BenchmarkController.class).init());
        dataGeneratorDB = new DataGeneratorDockerBuilder(new ExampleDockersBuilder(DataGenerator.class).init());
        taskGeneratorDB = new TaskGeneratorDockerBuilder(new ExampleDockersBuilder(TaskGenerator.class).init());

        evalStorageDB = new EvalStorageDockerBuilder(new ExampleDockersBuilder(LocalEvalStorage.class).init());

        systemAdapterDB = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class).init());
        evalModuleDB = new EvalModuleDockerBuilder(new ExampleDockersBuilder(EvalModule.class).init());
    }

    @Test
    @Ignore
    public void buildImages() throws Exception {

        init();

        AbstractDockerizer dockerizer = benchmarkDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = dataGeneratorDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = taskGeneratorDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = systemAdapterDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = evalModuleDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());
    }

    @Test
    public void checkHealth() throws Exception {

        Boolean useCachedImages = true;

        init();

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer.run();

        Component datagen = dataGeneratorDB.useCachedImage(useCachedImages).build();
        Component taskgen = taskGeneratorDB.useCachedImage(useCachedImages).build();
        Component evalstorage = new LocalEvalStorage();
        Component evalmodule = evalModuleDB.useCachedImage(useCachedImages).build();

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(datagen).dataGeneratorImageName(DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskgen).taskGeneratorImageName(TASKGEN_IMAGE_NAME)
                        .evalStorage(evalstorage).evalStorageImageName(EVAL_STORAGE_IMAGE_NAME)
                        .systemContainerId(systemAdapterDB.getImageName())
                        .evalModule(evalmodule).evalModuleImageName(EVALMODULE_IMAGE_NAME)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        Component benchmark = benchmarkDB.useCachedImage(useCachedImages).build();
        //Component benchmark = new DummyBenchmarkController();
        Component system = systemAdapterDB.useCachedImage(useCachedImages).build();
        //Component system = new DummySystemAdapter();

        componentsExecutor.submit(benchmark);
        componentsExecutor.submit(system, systemAdapterDB.getImageName());

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }






}
