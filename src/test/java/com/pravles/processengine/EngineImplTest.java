/*
 * Copyright 2025 Pravles Redneckoff
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pravles.processengine;

import com.pravles.processengine.api.ActivityFunction;
import com.pravles.processengine.api.ConditionFunction;
import com.pravles.processengine.impl.EngineImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

class EngineImplTest {
    @Test
    public void givenMyConditionTrue_whenRun_thenExecAct234()
            throws FileNotFoundException {
        // Given
        final EngineImpl sut = new EngineImpl();
        final InputStream is = new FileInputStream("src/test/resources" +
                "/process.1.fodg");
        final Map<String, Object> initCtx = new HashMap<>();
        initCtx.put("executedActivities", new ArrayList<String>());

        final Map<String, ActivityFunction> fnBindings = new HashMap<>();
        fnBindings.put("act1-fn", new TestActivity("act1-fn"));
        fnBindings.put("act2-fn", new TestActivity("act2-fn"));
        fnBindings.put("act3-fn", new TestActivity("act3-fn"));
        fnBindings.put("act4-fn", new TestActivity("act4-fn"));

        final Map<String, ConditionFunction> condFns = new HashMap<>();
        condFns.put("my-condition", new ConditionFunction() {
            @Override
            public Boolean apply(Map<String, Object> ctx) {
                return true;
            }
        });


        // When
        final Map<String, Object> actualCtx = sut.runWithoutSubprocesses(is, initCtx, fnBindings,
                condFns);

        // Then
        final List<String> executedActivities = (List<String>) actualCtx.get(
                "executedActivities");
        assertEquals(asList("act1-fn",
                "act2-fn", "act3-fn", "act4-fn"), executedActivities);
    }

    @Test
    public void givenMyConditionInContext_whenRun_thenExecAct234()
            throws FileNotFoundException {
        // Given
        final EngineImpl sut = new EngineImpl();
        final InputStream is = new FileInputStream("src/test/resources" +
                "/process.1.fodg");
        final Map<String, Object> initCtx = new HashMap<>();
        initCtx.put("executedActivities", new ArrayList<String>());
        initCtx.put("my-condition", Boolean.TRUE);

        final Map<String, ActivityFunction> fnBindings = new HashMap<>();
        fnBindings.put("act1-fn", new TestActivity("act1-fn"));
        fnBindings.put("act2-fn", new TestActivity("act2-fn"));
        fnBindings.put("act3-fn", new TestActivity("act3-fn"));
        fnBindings.put("act4-fn", new TestActivity("act4-fn"));

        final Map<String, ConditionFunction> condFns = new HashMap<>();

        // When
        final Map<String, Object> actualCtx = sut.runWithoutSubprocesses(is, initCtx, fnBindings,
                condFns);

        // Then
        final List<String> executedActivities = (List<String>) actualCtx.get(
                "executedActivities");
        assertEquals(asList("act1-fn",
                "act2-fn", "act3-fn", "act4-fn"), executedActivities);
    }
    @Test
    public void givenMyConditionTrue_whenRun_thenExecAct4()
            throws FileNotFoundException {
        // Given
        final EngineImpl sut = new EngineImpl();
        final InputStream is = new FileInputStream("src/test/resources" +
                "/process.1.fodg");
        final Map<String, Object> initCtx = new HashMap<>();
        initCtx.put("executedActivities", new ArrayList<String>());

        final Map<String, ActivityFunction> fnBindings = new HashMap<>();
        fnBindings.put("act1-fn", new TestActivity("act1-fn"));
        fnBindings.put("act2-fn", new TestActivity("act2-fn"));
        fnBindings.put("act3-fn", new TestActivity("act3-fn"));
        fnBindings.put("act4-fn", new TestActivity("act4-fn"));

        final Map<String, ConditionFunction> condFns = new HashMap<>();
        condFns.put("my-condition", new ConditionFunction() {
            @Override
            public Boolean apply(Map<String, Object> ctx) {
                return false;
            }
        });


        // When
        final Map<String, Object> actualCtx = sut.runWithoutSubprocesses(is, initCtx, fnBindings,
                condFns);

        // Then
        final List<String> executedActivities = (List<String>) actualCtx.get(
                "executedActivities");
        assertEquals(asList("act1-fn",
                "act4-fn"), executedActivities);

    }

    @Test
    public void givenCycle_whenRun_thenExecuteCorrectActivities()
            throws FileNotFoundException {
        // Given
        final EngineImpl sut = new EngineImpl();
        final InputStream is = new FileInputStream("src/test/resources" +
                "/process.2.fodg");
        final Map<String, Object> initCtx = new HashMap<>();
        initCtx.put("executedActivities", new ArrayList<String>());
        initCtx.put("counter", 0);

        final Map<String, ActivityFunction> fnBindings = new HashMap<>();
        fnBindings.put("read-counter", new TestActivity("read-counter") {
            @Override
            public Map<String, Object> apply(Map<String, Object> ctx) {
                return super.apply(ctx);
            }
        });
        fnBindings.put("increment-counter", new TestActivity("increment" +
                "-counter") {
            @Override
            public Map<String, Object> apply(final Map<String, Object> ctx) {
                final Map<String, Object> ctx2 = super.apply(ctx);
                final Integer oldValue = (Integer) ctx2.get("counter");
                ctx2.put("counter", oldValue + 1);
                return ctx2;
            }
        });

        final Map<String, ConditionFunction> condFns = new HashMap<>();
        condFns.put("Is counter greater than 2?", new ConditionFunction() {
            @Override
            public Boolean apply(Map<String, Object> ctx) {
                final Integer counter = (Integer) ctx.get("counter");
                return counter > 2;
            }
        });


        // When
        final Map<String, Object> actualCtx = sut.runWithoutSubprocesses(is, initCtx, fnBindings,
                condFns);

        // Then
        final List<String> executedActivities = (List<String>) actualCtx.get(
                "executedActivities");
        assertEquals(asList("read-counter",
                "increment-counter",
                "read-counter",
                "increment-counter",
                "read-counter",
                "increment-counter",
                "read-counter"), executedActivities);
        assertEquals(3, actualCtx.get("counter"));
    }

    @Test
    public void givenSubprocess_whenRun_thenExecuteRightActivities()
            throws FileNotFoundException {
        // Given
        final EngineImpl sut = new EngineImpl();
        final InputStream rootProcessInputStream =
                new FileInputStream("src/test/resources" +
                        "/process.3.fodg");
        final InputStream subprocessInputStream =
                new FileInputStream("src/test/resources" +
                "/process.1.fodg");

        final Map<String, InputStream> diagramInputStreamsByProcessIds =
                new HashMap<>();
        diagramInputStreamsByProcessIds.put(StringUtils.EMPTY,
                rootProcessInputStream);
        diagramInputStreamsByProcessIds.put("sub-process",
                subprocessInputStream);

        final Map<String, Object> initCtx = new HashMap<>();
        initCtx.put("executedActivities", new ArrayList<String>());

        final Map<String, ActivityFunction> fnBindings = asList(
                "parent.act1-fn",
                "parent.act2-fn",
                "act1-fn",
                "act2-fn",
                "act3-fn",
                "act4-fn",
                "parent.act4-fn")
                .stream()
                .collect(Collectors.toMap(
                        name -> name,
                        TestActivity::new));

        final Map<String, ConditionFunction> condFns = new HashMap<>();
        condFns.put("parent.my-condition", new ConditionFunction() {
            @Override
            public Boolean apply(Map<String, Object> ctx) {
                return true;
            }
        });

        condFns.put("my-condition", new ConditionFunction() {
            @Override
            public Boolean apply(Map<String, Object> ctx) {
                return true;
            }
        });


        // When
        final Map<String, Object> actualCtx =
                sut.runWithSubprocesses(diagramInputStreamsByProcessIds,
                        initCtx, fnBindings, condFns, StringUtils.EMPTY);

        // Then
        final List<String> executedActivities = (List<String>) actualCtx.get(
                "executedActivities");
        assertEquals(asList(
                "parent.act1-fn",
                "parent.act2-fn",
                "act1-fn",
                "act2-fn",
                "act3-fn",
                "act4-fn",
                "parent.act4-fn"),
                executedActivities);
    }

}