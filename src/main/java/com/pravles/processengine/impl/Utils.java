/*
 * Copyright 2025 Pravles Redneckoff
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pravles.processengine.impl;

import com.pravles.libreofficedraw.model.Vertex;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Utils {
    private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static GraphTraversalState.GraphTraversalStateBuilder
        copy(final GraphTraversalState oldState) {
        return GraphTraversalState.builder()
                .conditionFns(oldState.getConditionFns())
                .fnBindings(oldState.getFnBindings())
                .processGraphsByProcessIds(oldState.getProcessGraphsByProcessIds())
                .ctx(oldState.getCtx());
    }

    public static GraphTraversalState findNextEdge(DefaultDirectedGraph graph,
                                             Vertex curNode,
                                             GraphTraversalState state) {
        final Optional firstEdgeOpt =
                graph.outgoingEdgesOf(curNode).stream().findFirst();

        if (firstEdgeOpt.isEmpty()) {
            LOGGER.error("No outgoing edges found");
            return GraphTraversalState.builder()
                    .continueToWalkThroughGraph(false)
                    .build();
        }

        final Object firstEdge = firstEdgeOpt.get();

        return copy(state)
                .nextNodeToProcess((Vertex) graph.getEdgeTarget(firstEdge))
                .continueToWalkThroughGraph(true)
                .build();
    }

}
