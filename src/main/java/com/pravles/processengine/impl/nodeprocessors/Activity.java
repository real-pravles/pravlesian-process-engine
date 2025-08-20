/*
 * Copyright 2025 Pravles Redneckoff
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pravles.processengine.impl.nodeprocessors;

import clojure.lang.Keyword;
import com.pravles.processengine.api.ActivityFunction;
import com.pravles.processengine.impl.GraphTraversalState;
import com.pravles.processengine.impl.NodeProcessingInput;
import com.pravles.processengine.impl.NodeProcessor;
import com.pravles.processengine.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Activity implements NodeProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(Activity.class);
    @Override
    public GraphTraversalState apply(final NodeProcessingInput i) {
        final String fnName = (String) i.getCurNodeData()
                .get(Keyword.intern(
                "fn"));
        final ActivityFunction fn = i.getState().getFnBindings().get(fnName);

        if (fn == null) {
            LOGGER.error(String.format("Function '%s' is " +
                    "not bound", fnName));
            return Utils.findNextEdge(i.getGraph(),
                    i.getCurNode(), i.getState());
        }

        final Map<String, Object> newCtx = fn.apply(i.getState().getCtx());
        final GraphTraversalState result = Utils.findNextEdge(i.getGraph(),
                i.getCurNode(), i.getState());
        result.setCtx(newCtx);
        return result;
    }
}
