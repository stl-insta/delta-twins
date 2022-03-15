import java.util.*;

public class Partition {

    private Partition() {
    }

    // We use SortedMap to preserve time instants
    public static SortedMap<Integer, HashMap<Vertex, List<Vertex>>> computeAdjacencyListSuite(LinkStream ls) {
        SortedMap<Integer, HashMap<Vertex, List<Vertex>>> adjacencyListSuite = new TreeMap<>();
        // Create graph for non existing time
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
        int startTime = ls.getStartInstant();
        int endTime = ls.getEndInstant();
        var vertex = ls.getVertices();
        HashMap<Vertex, List<Vertex>> emptyGraph = new HashMap<>();
        for(var v: vertex) {
            emptyGraph.put(v, new ArrayList<>());
        }
        for(; startTime<endTime; startTime++){
            adjacencyListSuite.computeIfAbsent(startTime, k-> emptyGraph);
        }
        return adjacencyListSuite;
    }

    /**
     * P = {X1,X2,Xn}
     * Refine(P,N(x))
     * Time complexity: O(|V|+|E|)
     */
    private static Set<Set<Vertex>> refine(Map<Vertex, List<Vertex>> adjacencyList) {
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


    /**
     * Compute a map of time -> two modules from partitioning each adjacency entry
     * Each partition contains a pair of two modules after an enumeration
     * Time complexity: O(|V|*?)
     */
    public static SortedMap<Integer, Set<Set<Vertex>>> computeTwoModules(SortedMap<Integer, HashMap<Vertex, List<Vertex>>> adjacencyListSuite) {
        SortedMap<Integer, Set<Set<Vertex>>> twoModules = new TreeMap<>();
        for(var adjacencyList: adjacencyListSuite.entrySet()) {
            int time = adjacencyList.getKey();
            var partition = Partition.refine(adjacencyListSuite.get(time));

            // Partitions of size < 2 are rejected
            // Create new set for partitions of size > 2
            for (var p : partition) {
                if (p.size() < 2) continue;
                twoModules.computeIfAbsent(time, k -> new HashSet<>());
                twoModules.put(time, enumerateTwoModules(p));
            }
        }
        return twoModules;
    }

    /**
     * Extract two modules from a partition
     * A module may contain 2 or more vertex,
     * hence we need to enumerate each distinct
     * 2 vertices that might form an eternal twin.
     * Time complexity: O(|partition|^2)
     */
    private static Set<Set<Vertex>> enumerateTwoModules(Set<Vertex> partition) {
        Set<Set<Vertex>> twoModules = new HashSet<>();
        for(var p1: partition) {
            for(var p2: partition) {
                if(p1 == p2) continue;
                var couple = new HashSet<Vertex>();
                couple.add(p1);
                couple.add(p2);
                twoModules.add(couple);
            }
        }
        return twoModules;
    }
}
