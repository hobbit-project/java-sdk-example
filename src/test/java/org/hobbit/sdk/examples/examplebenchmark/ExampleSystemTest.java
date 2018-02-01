package org.hobbit.sdk.examples.examplebenchmark;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.common.PullBasedDockersBuilder;
import org.hobbit.sdk.examples.examplebenchmark.docker.ExampleDockersBuilder;
import org.hobbit.sdk.examples.examplebenchmark.system.SystemAdapter;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.SYSTEM_URI;
import static org.hobbit.sdk.examples.examplebenchmark.docker.ExampleDockersBuilder.SYSTEM_IMAGE_NAME;


/**
 * @author Pavel Smirnov
 *
 * This test shows how to debug your system under already published benchmark images (sml-v2 benchmark)
 * if docker images of benchmarkController components are available online
 *
 *
 */


public class ExampleSystemTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;


    String benchmarkImageName = "git.project-hobbit.eu:4567/smirnp/sml-v2/benchmark-controller";
    String dataGeneratorImageName = "git.project-hobbit.eu:4567/smirnp/sml-v2/data-generator";
    String taskGeneratorImageName = "git.project-hobbit.eu:4567/smirnp/sml-v2/task-generator";
    String evalStorageImageName = "git.project-hobbit.eu:4567/smirnp/sml-v2/eval-storage";
    String evalModuleImageName = "git.project-hobbit.eu:4567/smirnp/sml-v2/eval-module";

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


    public void init(boolean useCachedImages) throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI, createSystemParameters());

        benchmarkBuilder = new BenchmarkDockerBuilder(new PullBasedDockersBuilder(benchmarkImageName));
        dataGeneratorBuilder = new DataGenDockerBuilder(new PullBasedDockersBuilder(dataGeneratorImageName));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new PullBasedDockersBuilder(taskGeneratorImageName));
        evalStorageBuilder = new EvalStorageDockerBuilder(new PullBasedDockersBuilder(evalStorageImageName));
        evalModuleBuilder = new EvalModuleDockerBuilder(new PullBasedDockersBuilder(evalModuleImageName));

        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages));

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
        ((AbstractDockerizer)systemAdapter).prepareImage();
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

        //Here you can switch between dockerized (by default) and pure java code of your system
        //systemAdapter = new SystemAdapter();

        componentsExecutor.submit(benchmarkController);
        componentsExecutor.submit(systemAdapter, systemAdapterBuilder.getImageName());

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    public JenaKeyValue createBenchmarkParameters() {
        JenaKeyValue kv = new JenaKeyValue();
        //kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
        return kv;
    }

    private static JenaKeyValue createSystemParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        //kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
        return kv;
    }

}
