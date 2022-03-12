import java.util.*;

public class Partition {

    private Partition() {
    }

    // We use SortedMap to preserve time instants
    public static SortedMap<Integer, HashMap<Vertex, List<Vertex>>> computeAdjacencyListSuite(LinkStream ls) {
        SortedMap<Integer, HashMap<Vertex, List<Vertex>>> adjacencyListSuite = new TreeMap<>();
        // Adjacency List Construction
        for (TemporalEdge e : ls.getLinks()) {
            // Adjacency List
            int index = e.getT() - ls.getStartInstant();
            HashMap<Vertex, List<Vertex>> adjacencyList;
            if (!adjacencyListSuite.containsKey(index)) {
                adjacencyList = new HashMap<>();
            } else {
                adjacencyList = adjacencyListSuite.get(index);
            }

            // Edges of V
            List<Vertex> edgeUList;
            Vertex U = e.getU();
            if (!adjacencyList.containsKey(U)) {
                edgeUList = new ArrayList<>();
            } else {
                edgeUList = adjacencyList.get(U);
            }

            // Edges of V
            List<Vertex> edgeVList;
            Vertex V = e.getV();
            if (!adjacencyList.containsKey(V)) {
                edgeVList = new ArrayList<>();
            } else {
                edgeVList = adjacencyList.get(V);
            }

            edgeVList.add(U);
            adjacencyList.put(V, edgeVList);
            edgeUList.add(V);
            adjacencyList.put(U, edgeUList);
            adjacencyListSuite.put(index, adjacencyList);
        }
        return adjacencyListSuite;
    }

    /**
     * P = {X1,X2,Xn}
     * Refine(P,N(x))
     */
    public static Set<Set<Vertex>> refine(Map<Vertex, List<Vertex>> adjacencyList) {
        Set<Vertex> vertices = adjacencyList.keySet();
        Set<Set<Vertex>> refined = new HashSet<>();
        refined.add(vertices);
        for (var v : vertices) {
            var neighbor = adjacencyList.get(v);
            refined = _refine(refined, new HashSet<>(neighbor));
        }
        return refined;
    }

    /**
     * Refine(P,S) = {X1 inter S, X1 - S, ...., Xn inter S, Sn - s}
     */
    private static Set<Set<Vertex>> _refine(Set<Set<Vertex>> p, Set<Vertex> s) {
        Set<Set<Vertex>> q = new HashSet<>();
        for (Set<Vertex> x : p) {
            var intersection = new HashSet<>(x);
            var difference = new HashSet<>(x);
            intersection.retainAll(s);
            difference.removeAll(s);
            q.add(intersection);
            q.add(difference);
        }
        return q;
    }


    public static SortedMap<Integer, Set<Set<Vertex>>> computeTwoModules(SortedMap<Integer, HashMap<Vertex, List<Vertex>>> adjacencyListSuite) {
        SortedMap<Integer, Set<Set<Vertex>>> twoModules = new TreeMap<>();
        for(var adjacencyList: adjacencyListSuite.entrySet()) {
            int time = adjacencyList.getKey();
            var part = Partition.refine(adjacencyListSuite.get(time));
            // Extract twins : Partition of size 2
            for (var set : part) {
                if (set.size() != 2) continue;
                twoModules.computeIfAbsent(time, k -> new HashSet<>());
                twoModules.get(time).add(set);
            }
        }
        return twoModules;
    }
}
