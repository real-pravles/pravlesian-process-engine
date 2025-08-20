;
; Copyright 2025 Pravles Redneckoff
;
; Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
;
; The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
;
; THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
;

(ns lod-to-jgrapht)

(import 'org.jgrapht.graph.DefaultDirectedGraph)
(import 'org.jgrapht.graph.DefaultEdge)

(declare compose-graph)

(defn гав
  [diagram-data]
  (compose-graph diagram-data))

(defn compose-graph
  [diagram-data]
  (let [libre-office-draw-vertices (.getVertices diagram-data)
        libre-office-draw-edges (.getEdges diagram-data)
        vertices-by-ids (->> libre-office-draw-vertices
                             (map #(vector (.getId %) %))
                             (into {}))
        graph (DefaultDirectedGraph. DefaultEdge)]
    (doseq [v libre-office-draw-vertices] (.addVertex graph v))
    (doseq [e libre-office-draw-edges]
      (let [src (->> e
                     (.getSource)
                     (get vertices-by-ids))
            target (->> e
                        (.getTarget)
                        (get vertices-by-ids))
            label (.getLabel e)]
        (.addEdge graph src target e)))
    graph))
