/*
 * Copyright 2025 Pravles Redneckoff
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pravles.libreofficedraw;

import com.pravles.libreofficedraw.model.Edge;
import com.pravles.libreofficedraw.model.LibreOfficeDrawParsingResult;
import com.pravles.libreofficedraw.model.Vertex;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.xml.xpath.XPathConstants.NODESET;


public class LibreOfficeDrawParser {
    public LibreOfficeDrawParsingResult read(final InputStream is)
            throws ParserConfigurationException, XPathExpressionException,
            IOException, SAXException {
        // Load and parse the XML file
        final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(is);

        doc.getDocumentElement().normalize();

        return LibreOfficeDrawParsingResult.builder()
                .vertices(extractVertices(doc))
                .edges(extractEdges(doc))
                .build();
    }

    private List<Vertex> extractVertices(Document doc) throws XPathExpressionException {
        final List<Vertex> customShapeVertices = extractNodes(doc,
                "//*[local-name()='custom-shape']");
        final List<Vertex> groupVertices = extractNodes(doc,
                "//*[local-name()='g']");

        final List<Vertex> allVertices =
                new ArrayList<>(customShapeVertices.size());
        allVertices.addAll(customShapeVertices);
        allVertices.addAll(groupVertices);
        return allVertices;
    }

    private List<Edge> extractEdges(Document doc) throws XPathExpressionException {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        final XPathExpression xPathExpression = xPath.compile("//*[local-name()='connector']");

        final NodeList nodeList = (NodeList) xPathExpression.evaluate(doc,
                NODESET);
        final ArrayList<Edge> edges =
                new ArrayList<>(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            final NamedNodeMap nnm = node.getAttributes();

            edges.add(Edge.builder()
                            .source(extractAttribute(nnm, "draw:start-shape"))
                            .target(extractAttribute(nnm, "draw:end-shape"))
                            .label(extractChildNodeValue(node, "text:p"))
                    .build());
        }
        return edges;
    }

    private List<Vertex> extractNodes(final Document doc,
                                      final String expression)
            throws XPathExpressionException {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        final XPathExpression xPathExpression = xPath.compile(expression);

        final NodeList nodeList = (NodeList) xPathExpression.evaluate(doc,
                NODESET);
        final ArrayList<Vertex> vertices =
                new ArrayList<>(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            final NamedNodeMap nnm = node.getAttributes();

            vertices.add(Vertex.builder()
                    .id(extractAttribute(nnm, "xml:id"))
                    .name(extractAttribute(nnm, "draw:name"))
                    .description(extractChildNodeValue(node, "svg:desc"))
                    .build());
        }
        return vertices;
    }

    private static String extractChildNodeValue(Node node, String childNodeName) {
        final NodeList childNodes = node.getChildNodes();
        Node descNode = null;
        for (int j=0; (j < childNodes.getLength()) && (descNode == null); j++) {
            final Node curNode = childNodes.item(j);
            if (childNodeName.equals(curNode.getNodeName())) {
                descNode = curNode;
            }
        }
        final String description;
        if (descNode != null) {
            description = descNode.getTextContent();
        } else {
            description = "";
        }
        return description;
    }

    private static String extractAttribute(final NamedNodeMap nnm,
                                           final String attribute) {
        final Node nameNode = nnm.getNamedItem(attribute);
        final String name;
        if (nameNode != null) {
            name = nameNode.getTextContent();
        } else {
            name = "";
        }
        return name;
    }
}
