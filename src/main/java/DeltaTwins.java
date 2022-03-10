import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class DeltaTwins {

    //CHANGE DIRECTORY PATH AND VALUE OF GAMMA HERE
    private static String directory = "data/basic";
    private static int delta = 323;

    public static void main(String[] args) {
        File f = new File(directory);
        String[] pathList = f.list();

        if (pathList == null) {
            throw new RuntimeException("No file dataset found");
        }

        String[] dataSets = new String[pathList.length];

        for (int i = 0; i < pathList.length; i++) {
            dataSets[i] = directory + "/" + pathList[i];
        }

        // Aggregating all result
        List<String> header = new ArrayList<String>(Arrays.asList("Dataset", "Number of vertices", "Number of edges", "Number of time instants", "Time elapsed"));
        List<List<String>> results = new ArrayList<>();
        results.add(header);

        for (String filepath : dataSets) {
            System.out.println("COMPUTING FOR " + filepath);

            LinkStream ls = initiate(filepath);
            String vertices = String.valueOf(ls.getVertices().size());
            String edges = String.valueOf(ls.getLinks().size());
            String instants = String.valueOf(ls.getEndInstant() - ls.getStartInstant());
            System.out.println("Number of vertices : " + vertices + ", Number of edges : " + edges + ", for " + instants + " instants");
            // Get computed headers and results here
            List<String> computationResult = DeltaTwins.compute(ls);

            // Create line of result
            List<String> line = new ArrayList<>(Arrays.asList(filepath, vertices, edges, instants));
            results.add(line);
        }
        try {
            ResultBuilder resultBuilder = new ResultBuilder();
            String filename = "result-" + f.getName();
            resultBuilder.writeResult(results, filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not write result file");
        }

    }

    public static List<String> compute(LinkStream ls) {
        List<DeltaEdge> deltaTwins = new ArrayList<>();
        List<String> line = new ArrayList<>();
        line = computeEternalTwinsPartition(ls);
        System.out.println("Computation done ");
        //        deltaTwins = computeEternalTwinsNaively(ls, line);
//        deltaTwins = computeEternalTwinsMEI(ls, line);
//        deltaTwins = computeEternalTwinsMLEI(ls, line);
//        deltaTwins = computeDeltaTwinsNaively(ls, line, delta);
//        deltaTwins = computeDeltaTwinsMEI(ls, line, delta);
//        deltaTwins = computeDeltaTwinsMLEI(ls, line, delta);

        return line;
    }

    static private LinkStream initiate(String filePath) {
        FileParser fp = null;
        try {
            fp = new FileParser(filePath);
        } catch (IOException e) {
            System.err.println("C'est cassé");
        }

        assert fp != null;
        return fp.getLs();

    }

    public static List<String> computeEternalTwinsPartition(LinkStream ls) {
        List<DeltaEdge> eternalTwins = new ArrayList<>();
        HashMap<Integer, HashMap<Vertex, List<Vertex>>> adjacencyListSuite = new HashMap<>();
        // Initialization
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
            if(!adjacencyList.containsKey(U)) {
                edgeUList = new ArrayList<>();
            } else {
                edgeUList = adjacencyList.get(U);
            }

            // Edges of V
            List<Vertex> edgeVList;
            Vertex V = e.getV();
            if(!adjacencyList.containsKey(V)) {
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


        return new ArrayList<String>();
    }

    public static ArrayList<DeltaEdge> computeEternalTwinsNaively(LinkStream ls, String line) {

        System.out.println("COMPUTING ETERNAL TWINS NAIVELY");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        try {
            Date startTime = new Date();

            deltaTwins = TwinAlgorithms.naivelyComputeEternalTwins(ls);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line = line.concat(deltaTwins.size() + "," + timeElapsed + ",");
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line = line.concat(",OUT OF MEMORY,");
        }
        System.out.println("We have " + deltaTwins.size() + " eternal twins");
        return deltaTwins;
    }

    public static ArrayList<DeltaEdge> computeEternalTwinsMEI(LinkStream ls, String line) {

        System.out.println("COMPUTING ETERNAL TWINS USING EDGES ITERATION");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.computeEternalTwinsByEdgesIteration(ls);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line = line.concat(timeElapsed + ",");
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line = line.concat("OUT OF MEMORY,");
        }
        System.out.println("We have " + deltaTwins.size() + " eternal twins");
        return deltaTwins;
    }

    public static List<String> computeEternalTwinsMLEI(LinkStream ls) {
        TwinAlgorithms algorithms = new TwinAlgorithms();
        List<String> line = new ArrayList<>();
        try {
            line = ComputationFactory.launchComputation(ls, "ETERNAL TWINS MLEI", algorithms, "computeEternalTwinsByEdgesIterationWithoutMatrices");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static ArrayList<DeltaEdge> computeDeltaTwinsNaively(LinkStream ls, String line, int delta) {

        System.out.println("COMPUTING " + delta + "-TWINS NAIVELY");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();

        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.naivelyComputeDeltaTwins(ls, delta);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line = line.concat(deltaTwins.size() + "," + timeElapsed + ",");
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line = line.concat(",OUT OF MEMORY,");
        }
        System.out.println("We have " + deltaTwins.size() + " " + delta + "-twins");
        return deltaTwins;
    }

    public static ArrayList<DeltaEdge> computeDeltaTwinsMEI(LinkStream ls, String line, int delta) {

        System.out.println("COMPUTING " + delta + "-TWINS USING EDGES ITERATION");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();

        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.computeDeltaTwinsByEdgesIteration(ls, delta);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line = line.concat(timeElapsed + ",");
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line = line.concat("OUT OF MEMORY,");
        }
        System.out.println("We have " + deltaTwins.size() + " " + delta + "-twins");
        return deltaTwins;
    }

    public static ArrayList<DeltaEdge> computeDeltaTwinsMLEI(LinkStream ls, String line, int delta) {

        System.out.println("COMPUTING " + delta + "-TWINS USING EDGES ITERATION WITHOUT MATRICES");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();

        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.computeDeltaTwinsByEdgesIterationWithoutMatrices(ls, delta);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line = line.concat(timeElapsed + ",,");

        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line = line.concat("OUT OF MEMORY,,");
        }
        System.out.println("We have " + deltaTwins.size() + " " + delta + "-twins");
        return deltaTwins;
    }
}
