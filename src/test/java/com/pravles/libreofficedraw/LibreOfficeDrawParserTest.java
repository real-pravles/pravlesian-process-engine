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
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class LibreOfficeDrawParserTest {
    @Test
    public void test() {
        try (final InputStream is =
                     FileUtils.openInputStream(new File("src/test/resources" +
                             "/process_diagram.fodg.xml"))) {
            // Given
            final LibreOfficeDrawParser sut = new LibreOfficeDrawParser();

            // When
            final LibreOfficeDrawParsingResult actualResult = sut.read(is);

            // Then
            final List<Vertex> vertices = actualResult.getVertices();
            final List<Edge> edges = actualResult.getEdges();

            assertEquals(9, vertices.size());
            assertEquals(7, edges.size());

            assertTrue(vertices.contains(Vertex.builder()
                    .id("id1")
                    .name("process1")
                    .description("Fuck the West.")
                    .build()));
            assertTrue(vertices.contains(Vertex.builder()
                    .id("id2")
                    .name("process2")
                    .description("From the river to the sea, Palestine shall be free.")
                    .build()));
            assertTrue(vertices.contains(Vertex.builder()
                    .id("")
                    .name("")
                    .description("")
                    .build()));
            assertTrue(vertices.contains(Vertex.builder()
                    .id("id5")
                    .name("process3")
                    .description("Description of process 3.\n" + "Line 2.\n" + "Line 3.")
                    .build()));
            assertTrue(vertices.contains(Vertex.builder()
                    .id("id3")
                    .name("gateway1")
                    .description("")
                    .build()));
            assertTrue(vertices.contains(Vertex.builder()
                    .id("id4")
                    .name("gateway 1")
                    .description("")
                    .build()));

            assertTrue(edges.contains(Edge.builder()
                    .source("id1")
                    .target("id2")
                    .label("")
                    .build()));
            assertTrue(edges.contains(Edge.builder()
                    .source("id2")
                    .target("id3")
                    .label("")
                    .build()));
            assertTrue(edges.contains(Edge.builder()
                    .source("id3")
                    .target("id4")
                    .label("Another arrow")
                    .build()));
            assertTrue(edges.contains(Edge.builder()
                    .source("id3")
                    .target("id5")
                    .label("Arrow from gateway to process 3")
                    .build()));
            assertTrue(edges.contains(Edge.builder()
                    .source("id5")
                    .target("id4")
                    .label("")
                    .build()));
            assertTrue(edges.contains(Edge.builder()
                    .source("id6")
                    .target("id1")
                    .label("")
                    .build()));
            assertTrue(edges.contains(Edge.builder()
                    .source("id4")
                    .target("id7")
                    .label("")
                    .build()));
        } catch (final IOException | XPathExpressionException
                | SAXException
                | ParserConfigurationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}