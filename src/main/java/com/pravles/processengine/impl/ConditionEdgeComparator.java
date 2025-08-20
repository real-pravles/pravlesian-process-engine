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

import com.pravles.libreofficedraw.model.Edge;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class ConditionEdgeComparator implements Comparator<Pair<String, Edge>> {
    @Override
    public int compare(final Pair<String, Edge> o1,
                       final Pair<String, Edge> o2) {
        final int idx1 = calculateIndex(o1);
        final int idx2 = calculateIndex(o2);
        return idx1 - idx2;
    }

    private int calculateIndex(Pair<String, Edge> p) {
        if (p == null) {
            return 0;
        }

        if (p.getLeft() == null) {
            return 0;
        }

        if ("false".equalsIgnoreCase(p.getLeft())) {
            return 10;
        }
        return 0;
    }
}
