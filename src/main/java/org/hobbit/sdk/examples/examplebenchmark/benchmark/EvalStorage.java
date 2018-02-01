package org.hobbit.sdk.examples.examplebenchmark.benchmark;

import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;
import org.hobbit.sdk.ResultPairImpl;
import org.hobbit.sdk.SerializableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class EvalStorage extends AbstractEvaluationStorage {
    private static final Logger logger = LoggerFactory.getLogger(EvalStorage.class);
    protected Exception exception;

    private static final int MAX_OBJECT_SIZE = 100 * 1024; // 100mb

    private final List<Result> actualResponses = new ArrayList<>();
    private final List<Result> expectedResponses = new ArrayList<>();


    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
    }

    @Override
    public void receiveExpectedResponseData(String s, long l, byte[] bytes) {
        logger.trace("receiveExpectedResponseData()->{}",new String(bytes));
        int actualSize = bytes.length / 1024;
        expectedResponses.add(new SerializableResult(l,bytes));
    }

    @Override
    public void receiveResponseData(String s, long l, byte[] bytes) {
        int actualSize = bytes.length / 1024;
        logger.trace("receiveResponseData()->{}",new String(bytes));
        actualResponses.add(new SerializableResult(l,bytes));
    }

    @Override
    protected Iterator<ResultPair> createIterator(){
        logger.debug("createIterator()");
        String test="123";

        List<ResultPair> ret = new ArrayList<>();
        for(int i = 0; i< expectedResponses.size(); i++)
            ret.add(new ResultPairImpl(expectedResponses.get(i), actualResponses.get(i)));

        return ret.iterator();
    }



}
