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

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import com.pravles.libreofficedraw.LibreOfficeDrawParser;
import com.pravles.libreofficedraw.model.LibreOfficeDrawParsingResult;
import com.pravles.libreofficedraw.model.Vertex;
import com.pravles.processengine.api.ActivityFunction;
import com.pravles.processengine.api.ConditionFunction;
import com.pravles.processengine.api.Engine;
import com.pravles.processengine.impl.nodeprocessors.Activity;
import com.pravles.processengine.impl.nodeprocessors.CallSubProcess;
import com.pravles.processengine.impl.nodeprocessors.End;
import com.pravles.processengine.impl.nodeprocessors.FindNextEdge;
import com.pravles.processengine.impl.nodeprocessors.GatewayOpen;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EngineImpl implements Engine {
    private final static Logger LOGGER =
            LoggerFactory.getLogger(EngineImpl.class);
    private final Map<Keyword, NodeProcessor> nodeProcessorsByTypes;

    public EngineImpl() {
        this.nodeProcessorsByTypes = new HashMap<>();
        final FindNextEdge findNextEdge = new FindNextEdge();
        this.nodeProcessorsByTypes.put(Keyword.intern("start"),
                findNextEdge);
        this.nodeProcessorsByTypes.put(Keyword.intern("activity"), new Activity());
        this.nodeProcessorsByTypes.put(Keyword.intern("gateway-open"), new GatewayOpen());
        this.nodeProcessorsByTypes.put(Keyword.intern("gateway-close"), findNextEdge);
        this.nodeProcessorsByTypes.put(Keyword.intern("end"), new End());
        this.nodeProcessorsByTypes.put(Keyword.intern("call-subprocess"),
                new CallSubProcess(this));
    }

    @Override
    public Map<String, Object> runWithoutSubprocesses(
            final InputStream diagramInputStream,
            final Map<String, Object> initCtx,
            final Map<String, ActivityFunction> fnBindings,
            final Map<String, ConditionFunction>
                    conditionFns) {
        final Map<String, InputStream> diagramInputStreamsByProcessIds = new HashMap<>();
        diagramInputStreamsByProcessIds.put(StringUtils.EMPTY,
                diagramInputStream);
        return runWithSubprocesses(diagramInputStreamsByProcessIds,
                initCtx, fnBindings, conditionFns, StringUtils.EMPTY);
    }

    @Override
    public Map<String, Object> runWithSubprocesses(
            final Map<String, InputStream> diagramInputStreamsByProcessIds,
            final Map<String, Object> initCtx,
            final Map<String, ActivityFunction> activityFns,
            final Map<String, ConditionFunction> conditionFns,
            final String processId) {
        final Map<String, DefaultDirectedGraph> processGraphsByProcessIds = turnXmlFilesIntoGraphs(diagramInputStreamsByProcessIds);

        return runGraphWithSubprocesses(processGraphsByProcessIds, initCtx, activityFns, conditionFns, processId);
    }

    @Override
    public Map<String, Object> runGraphWithSubprocesses(
        Map<String, DefaultDirectedGraph> processGraphsByProcessIds,
        Map<String, Object> initCtx,
        Map<String, ActivityFunction> activityFns,
        Map<String, ConditionFunction> conditionFns,
        String processId) {
        final DefaultDirectedGraph processGraph =
                processGraphsByProcessIds.get(processId);

        final Optional<Vertex> startNodeOpt =
                findStartNode(processGraph);

        if (startNodeOpt.isEmpty()) {
            LOGGER.error("No start node found");
            return initCtx;
        }

        GraphTraversalState state = GraphTraversalState
                .builder()
                .fnBindings(activityFns)
                .conditionFns(conditionFns)
                .ctx(initCtx)
                .nextNodeToProcess(startNodeOpt.get())
                .continueToWalkThroughGraph(true)
                .curProcessId(processId)
                .processGraphsByProcessIds(processGraphsByProcessIds)
                .build();

        while (state.isContinueToWalkThroughGraph()) {
            final Vertex curNode = state.getNextNodeToProcess();
            final Map<String, Object> curNodeData =
                    parseClojureMap(curNode.getDescription());
            final Keyword type = (Keyword) curNodeData
                    .get(Keyword.intern("type"));
            final NodeProcessor nodeProcessor =
                    nodeProcessorsByTypes.get(type);
            state = nodeProcessor.apply(NodeProcessingInput.builder()
                    .curNode(curNode)
                    .curNodeData(curNodeData)
                    .graph(processGraph)
                    .state(state)
                    .build());
        }

        return state.getCtx();
    }

    private Map<String, DefaultDirectedGraph> turnXmlFilesIntoGraphs(
            final Map<String, InputStream> diagramInputStreamsByProcessIds) {
        final Map<String, DefaultDirectedGraph> processGraphsByProcessIds =
                new HashMap<>();

        for (final Map.Entry<String, InputStream> entry :
                diagramInputStreamsByProcessIds.entrySet()) {
            try {
                processGraphsByProcessIds.put(entry.getKey(),
                        turnDiagramIntoGraph(entry.getValue()));
            } catch (XPathExpressionException
                    | ParserConfigurationException
                    | IOException
                    | SAXException e) {
                LOGGER.error(String.format("Error while parsing process " +
                        "diagram '%s'", entry.getKey()), e);
            }
        }

        return processGraphsByProcessIds;
    }

    private Optional findStartNode(DefaultDirectedGraph graph) {
        return graph.vertexSet()
                .stream()
                .filter(node -> (node instanceof Vertex))
                .filter(node -> {
                    final Vertex v = (Vertex) node;
                    final Map<String, Object> data =
                            parseClojureMap(v.getDescription());
                    if (data == null) {
                        return false;
                    }
                    final Object type = data.get(Keyword.intern("type"));

                    return Keyword.intern("start").equals(type);
                })
                .findFirst();
    }

    private static DefaultDirectedGraph turnDiagramIntoGraph(InputStream diagramInputStream) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        final LibreOfficeDrawParser lodParser = new LibreOfficeDrawParser();
        final LibreOfficeDrawParsingResult lodData =
                lodParser.read(diagramInputStream);

        final DefaultDirectedGraph graph =
                (DefaultDirectedGraph) ShariysDog.woof("lod-to-jgrapht")
                        .invoke(lodData);
        return graph;
    }

    private Map<String, Object> parseClojureMap(final String clojureCode) {
        final IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("clojure.edn"));
        final IFn readString = Clojure.var("clojure.edn", "read-string");
        return (Map<String, Object>) readString.invoke(clojureCode);
    }
}
