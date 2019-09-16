/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.query.builder.support.builder;

import com.itfsw.query.builder.MongodbQueryBuilderFactory;
import com.itfsw.query.builder.other.FileHelper;
import com.itfsw.query.builder.support.model.result.MongodbQueryResult;
import com.itfsw.query.builder.support.utils.spring.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/11/2 17:57
 * ---------------------------------------------------------------------------
 */
public class MongodbBuilderTest {
    private static MongodbBuilder builder;

    @BeforeClass
    public static void init() {
        MongodbQueryBuilderFactory factory = new MongodbQueryBuilderFactory();
        builder = factory.builder();
    }

    /**
     * equal 操作
     */
    @Test
    public void testOperatorEqual() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-equal.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":\"Mistic\"}]}"
        );
    }

    /**
     * not equal 操作
     */
    @Test
    public void testOperatorNotEqual() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-not-equal.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$ne\":\"Mistic\"}}]}"
        );
    }

    /**
     * in 操作
     */
    @Test
    public void testOperatorIn() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-in.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$in\":[1,5,10]}}]}"
        );
    }

    /**
     * not in 操作
     */
    @Test
    public void testOperatorNotIn() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-not-in.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$nin\":[1,5,10]}}]}"
        );
    }

    /**
     * less 操作
     */
    @Test
    public void testOperatorLess() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-less.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$lt\":50}}]}"
        );
    }

    /**
     * less or equal 操作
     */
    @Test
    public void testOperatorLessOrEqual() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-less-or-equal.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$lte\":50}}]}"
        );
    }

    /**
     * greater 操作
     */
    @Test
    public void testOperatorGreater() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-greater.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$gt\":50}}]}"
        );
    }

    /**
     * greater or equal 操作
     */
    @Test
    public void testOperatorGreaterOrEqual() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-greater-or-equal.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$gte\":50}}]}"
        );
    }

    /**
     * between 操作
     */
    @Test
    public void testOperatorBetween() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-between.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$gte\":5,\"$lte\":10}}]}"
        );
    }

    /**
     * not between 操作
     */
    @Test
    public void testOperatorNotBetween() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-not-between.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"age\":{\"$lt\":5,\"$gt\":10}}]}"
        );
    }


    /**
     * begins with 操作
     */
    @Test
    public void testOperatorBeginsWith() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-begins-with.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$regex\":\"^Mistic\"}}]}"
        );
    }

    /**
     * not begins with 操作
     */
    @Test
    public void testOperatorNotBeginsWith() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-not-begins-with.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$regex\":\"^(?!Mistic)\"}}]}"
        );
    }

    /**
     * contains 操作
     */
    @Test
    public void testOperatorContains() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-contains.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$regex\":\"Mistic\"}}]}"
        );
    }

    /**
     * not contains 操作
     */
    @Test
    public void testOperatorNotContains() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-not-contains.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$regex\":\"^((?!Mistic).)*$\",\"$options\":\"s\"}}]}"
        );
    }

    /**
     * ends with 操作
     */
    @Test
    public void testOperatorEndsWith() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-ends-with.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$regex\":\"Mistic$\"}}]}"
        );
    }

    /**
     * not ends with 操作
     */
    @Test
    public void testOperatorNotEndsWith() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-not-ends-with.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$regex\":\"(?<!Mistic)$\"}}]}"
        );
    }

    /**
     * is empty 操作
     */
    @Test
    public void testOperatorIsEmpty() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-is-empty.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":\"\"}]}"
        );
    }

    /**
     * is not empty 操作
     */
    @Test
    public void testOperatorIsNotEmpty() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-is-not-empty.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$ne\":\"\"}}]}"
        );
    }

    /**
     * is null 操作
     */
    @Test
    public void testOperatorIsNull() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-is-null.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":null}]}"
        );
    }

    /**
     * is not null 操作
     */
    @Test
    public void testOperatorIsNotNull() throws IOException {
        String json = FileHelper.getStringFrom("tasks/operator-is-not-null.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":{\"$ne\":null}}]}"
        );
    }

    /**
     * and 操作
     */
    @Test
    public void testConditionAnd() throws IOException {
        String json = FileHelper.getStringFrom("tasks/condition-and-1.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$and\":[{\"username\":\"Mistic\"}]}"
        );

        json = FileHelper.getStringFrom("tasks/condition-and-more.json");
        result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$and\":[{\"username\":\"Mistic\"},{\"age\":10}]}"
        );
    }

    /**
     * or 操作
     */
    @Test
    public void testConditionOr() throws IOException {
        String json = FileHelper.getStringFrom("tasks/condition-or-1.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":\"Mistic\"}]}"
        );

        json = FileHelper.getStringFrom("tasks/condition-or-more.json");
        result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$or\":[{\"username\":\"Mistic\"},{\"age\":10}]}"
        );
    }

    /**
     * not 操作
     */
    @Test
    public void testConditionNot() throws IOException {
        String json = FileHelper.getStringFrom("tasks/condition-not-and-1.json");
        MongodbQueryResult result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$nor\":[{\"$and\":[{\"username\":\"Mistic\"}]}]}"
        );

        json = FileHelper.getStringFrom("tasks/condition-not-and-more.json");
        result = builder.build(json);

        Assert.assertEquals(
                StringUtils.trimAllWhitespace(result.toString()),
                "{\"$nor\":[{\"$and\":[{\"username\":\"Mistic\"},{\"age\":10}]}]}"
        );
    }
}