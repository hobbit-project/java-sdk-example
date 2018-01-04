package org.hobbit.sdk.examples;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.LocalEvalStorage;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.docker.ExampleDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkTest extends EnvironmentVariablesWrapper {

    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    Component benchmark = new BenchmarkController();
    Component datagen = new DataGenerator();
    Component taskgen = new TaskGenerator();
    Component evalstorage = new LocalEvalStorage();
    Component system = new SystemAdapter();
    Component evalmodule = new EvalModule();


    @Test
    public void checkHealth() throws Exception {

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .build();
        rabbitMqDockerizer.run();

        String systemContainerId = "exampleSystem";
        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(datagen).dataGeneratorImageName(DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskgen).taskGeneratorImageName(TASKGEN_IMAGE_NAME)
                        .evalStorage(evalstorage).evalStorageImageName(EVAL_STORAGE_IMAGE_NAME)
                        .evalModule(evalmodule).evalModuleImageName(EVALMODULE_IMAGE_NAME)
                        .systemContainerId(systemContainerId)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(benchmark);
        componentsExecutor.submit(system, systemContainerId);

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        Assert.assertFalse(componentsExecutor.anyExceptions());
        rabbitMqDockerizer.stop();
    }



}
