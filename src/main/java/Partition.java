import java.util.*;

public class Partition {

    private Partition() {}

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
}
