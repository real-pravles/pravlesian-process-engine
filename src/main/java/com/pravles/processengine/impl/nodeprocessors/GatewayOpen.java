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

import com.pravles.libreofficedraw.model.Edge;
import com.pravles.libreofficedraw.model.Vertex;
import com.pravles.processengine.api.ConditionFunction;
import com.pravles.processengine.impl.ConditionEdgeComparator;
import com.pravles.processengine.impl.GraphTraversalState;
import com.pravles.processengine.impl.NodeProcessingInput;
import com.pravles.processengine.impl.NodeProcessor;
import com.pravles.processengine.impl.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GatewayOpen implements NodeProcessor {
    private final static Logger LOGGER =
            LoggerFactory.getLogger(GatewayOpen.class);

    @Override
    public GraphTraversalState apply(final NodeProcessingInput in) {
        final List<Pair<String, Edge>> conditionsEdges =
                new ArrayList<>(in.getGraph().outDegreeOf(in.getCurNode()));
        in.getGraph().outgoingEdgesOf(in.getCurNode())
                .stream()
                .forEach(e -> {
                    final Edge edge = (Edge) e;
                    conditionsEdges.add(Pair.of(edge.getLabel(),
                            edge));
                });

        Collections.sort(conditionsEdges,
                new ConditionEdgeComparator());

        Edge nextEdgeToProcess = null;
        boolean matchingConditionFound = false;
        int i=0;

        while (!matchingConditionFound && (i < conditionsEdges.size())) {
            final Pair<String, Edge> curTuple =
                    conditionsEdges.get(i);
            final String fnName = curTuple.getLeft();

            Boolean evalResult = false;

            if ("false".equalsIgnoreCase(fnName)) {
                evalResult = true;
            } else if (in.getState().getConditionFns().containsKey(fnName)) {
                final ConditionFunction fn = in.getState().getConditionFns().get(fnName);
                evalResult = fn.apply(in.getState().getCtx());
            } else if (in.getState().getCtx().containsKey(fnName)) {
                evalResult = (Boolean) in.getState().getCtx().get(fnName);
            } else {
                LOGGER.error(String.format("'%s' is neither a condition " +
                        "nor a key in the contxt", fnName));
                evalResult = null;
            }
            if (Boolean.TRUE.equals(evalResult)) {
                matchingConditionFound = true;
                nextEdgeToProcess = curTuple.getRight();
            } else {
                i++;
            }
        }

        if (nextEdgeToProcess != null) {

            return Utils.copy(in.getState())
                    .nextNodeToProcess((Vertex) in.getGraph().getEdgeTarget(nextEdgeToProcess))
                    .continueToWalkThroughGraph(true)
                    .build();
        } else {
            return GraphTraversalState.builder()
                    .continueToWalkThroughGraph(false)
                    .build();
        }
    }
}
