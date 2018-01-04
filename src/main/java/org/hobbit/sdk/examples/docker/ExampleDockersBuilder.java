package org.hobbit.sdk.examples.docker;


import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

/**
 * @author Pavel Smirnov
 */

public class ExampleDockersBuilder extends DynamicDockerFileBuilder {

    //public static String GIT_REPO_PATH = "git.project-hobbit.eu:4567/yourloginname/";
    public static String GIT_REPO_PATH = "";
    public static String PROJECT_NAME = "sdk-example";

    //use these constants within BenchmarkController
    public static final String DATAGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-datagen";
    public static final String TASKGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-taskgen";
    //public static final String EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-eval-storage";
    public static final String EVAL_STORAGE_IMAGE_NAME = "git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.5";
    public static final String EVALMODULE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-eval-module";

    public ExampleDockersBuilder(Class runnerClass){
        super("ExampleDockersBuilder");
        //will be used for all images ()
        imageNamePrefix(GIT_REPO_PATH+PROJECT_NAME+"-");
        //user-friendly name for searching in logs
        containerName(runnerClass.getSimpleName());
        //temp docker file will be created there
        buildDirectory("target");
        //should be packaged will all dependencies (via 'mvn package' command)
        jarFileName("example-benchmark-1.0.jar");
        //will be placed in temp dockerFile
        dockerWorkDir("/usr/src/"+PROJECT_NAME);
        //will be placed in temp dockerFile
        runnerClass(org.hobbit.core.run.ComponentStarter.class, runnerClass);
    }


    @Override
    public BuildBasedDockerizer build() throws Exception{
        throw new Exception("Direct build is prohibited! Allowed to be used only as argument for other dockerBuilders");
    };
}
