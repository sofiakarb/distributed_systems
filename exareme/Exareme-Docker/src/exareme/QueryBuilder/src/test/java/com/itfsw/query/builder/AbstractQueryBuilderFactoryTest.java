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

package com.itfsw.query.builder;

import com.itfsw.query.builder.exception.FilterAddException;
import com.itfsw.query.builder.exception.ParserAddException;
import com.itfsw.query.builder.other.Test2Filter;
import com.itfsw.query.builder.other.Test2Parser;
import com.itfsw.query.builder.other.TestFilter;
import com.itfsw.query.builder.other.TestParser;
import com.itfsw.query.builder.support.filter.ValidateFilter;
import com.itfsw.query.builder.support.parser.sql.EqualRuleParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/11/2 17:30
 * ---------------------------------------------------------------------------
 */
public class AbstractQueryBuilderFactoryTest {
    private TestFilter testFilter = new TestFilter();
    private TestParser testParser = new TestParser();

    @Test
    public void addFilter() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addFilter(testFilter);
        Assert.assertEquals(factory.getFilters().get(factory.getFilters().size() - 1), testFilter);
    }

    @Test
    public void addFilterBefore() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addFilterBefore(testFilter, ValidateFilter.class);
        Assert.assertEquals(getIndexOfClass(factory.getFilters(), testFilter.getClass()) + 1, getIndexOfClass(factory.getFilters(), ValidateFilter.class));
    }

    @Test
    public void addFilterAfter() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addFilterAfter(testFilter, ValidateFilter.class);
        Assert.assertEquals(getIndexOfClass(factory.getFilters(), testFilter.getClass()) - 1, getIndexOfClass(factory.getFilters(), ValidateFilter.class));
    }

    @Test
    public void addFilterAt() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();

        int index = getIndexOfClass(factory.getFilters(), ValidateFilter.class);

        factory.addFilterAt(testFilter, ValidateFilter.class);
        Assert.assertEquals(-1, getIndexOfClass(factory.getFilters(), ValidateFilter.class));
        Assert.assertEquals(index, getIndexOfClass(factory.getFilters(), testFilter.getClass()));
    }

    @Test
    public void addParser() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addRuleParser(testParser);
        Assert.assertEquals(factory.getRuleParsers().get(factory.getRuleParsers().size() - 1), testParser);
    }

    @Test
    public void addParserBefore() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addRuleParserBefore(testParser, EqualRuleParser.class);
        Assert.assertEquals(getIndexOfClass(factory.getRuleParsers(), testParser.getClass()) + 1, getIndexOfClass(factory.getRuleParsers(), EqualRuleParser.class));
    }

    @Test
    public void addParserAfter() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addRuleParserAfter(testParser, EqualRuleParser.class);
        Assert.assertEquals(getIndexOfClass(factory.getRuleParsers(), testParser.getClass()) - 1, getIndexOfClass(factory.getRuleParsers(), EqualRuleParser.class));
    }

    @Test
    public void addParserAt() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();

        int index = getIndexOfClass(factory.getRuleParsers(), EqualRuleParser.class);

        factory.addRuleParserAt(testParser, EqualRuleParser.class);
        Assert.assertEquals(-1, getIndexOfClass(factory.getRuleParsers(), EqualRuleParser.class));
        Assert.assertEquals(index, getIndexOfClass(factory.getRuleParsers(), testParser.getClass()));
    }

    /**
     * 测试替换没有加入的Filter
     */
    @Test(expected = FilterAddException.class)
    public void testAddFilterBeforeUnknownClass(){
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addFilterBefore(testFilter, Test2Filter.class);
    }

    /**
     * 测试替换没有加入的Filter
     */
    @Test(expected = FilterAddException.class)
    public void testAddFilterAtUnknownClass(){
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addFilterAt(testFilter, Test2Filter.class);
    }

    /**
     * 测试替换没有加入的Filter
     */
    @Test(expected = FilterAddException.class)
    public void testAddFilterAfterUnknownClass(){
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addFilterAfter(testFilter, Test2Filter.class);
    }

    /**
     * 测试替换没有加入的Parser
     */
    @Test(expected = ParserAddException.class)
    public void addParserBeforeUnknownClass() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addRuleParserBefore(testParser, Test2Parser.class);
    }

    /**
     * 测试替换没有加入的Parser
     */
    @Test(expected = ParserAddException.class)
    public void addParserAtUnknownClass() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addRuleParserAt(testParser, Test2Parser.class);
    }

    /**
     * 测试替换没有加入的Parser
     */
    @Test(expected = ParserAddException.class)
    public void addParserAfterUnknownClass() throws Exception {
        AbstractQueryBuilderFactory factory = new SqlQueryBuilderFactory();
        factory.addRuleParserAfter(testParser, Test2Parser.class);
    }

    /**
     * get class index of list
     * @param list
     * @param cls
     * @return
     */
    private int getIndexOfClass(List list, Class cls) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getClass().equals(cls)) {
                return i;
            }
        }
        return -1;
    }
}