import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.exit;

public class DeltaTwins {
    private static int delta = 0;
    private static String directory;

    private static List<String>
            header = new ArrayList<>(Arrays.asList("Dataset", "Number of vertices", "Number of edges", "Number of time instants"));

    public static void main(String[] args) {
        directory = args[0];
        try {
            delta = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            delta = 0;
        }

        if(directory == null) {
            System.err.println("No dataset provided");
            exit(1);
        }

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
        List<List<String>> results = new ArrayList<>();
        // Add computation correct header here
        header.add("Partition Eternal-twins computation time (ms)");
        header.add("MEI Eternal-twins computation time (ms)");
        header.add("MLEI Eternal-twins computation time (ms)");
        results.add(header);

        for (String filepath : dataSets) {
            System.out.println("COMPUTING FOR " + filepath);

            LinkStream ls = initiate(filepath);
            String vertices = String.valueOf(ls.getVertices().size());
            String edges = String.valueOf(ls.getLinks().size());
            String instants = String.valueOf(ls.getEndInstant() - ls.getStartInstant());
            System.out.println("Number of vertices : " + vertices + ", Number of edges : " + edges + ", for " + instants + " instants");

            List<String> line = new ArrayList<>(Arrays.asList(filepath, vertices, edges, instants));
            DeltaTwins.compute(ls, line);

            // Create line of result
            results.add(line);
        }
        try {
            ResultBuilder resultBuilder = new ResultBuilder();
            String filename = "result-" + f.getName() + ".csv";
            resultBuilder.writeResult(results, filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not write result file");
        }

    }

    public static void compute(LinkStream ls, List<String> line) {
        List<List<DeltaEdge>> eternalTwins = new ArrayList<>();
        eternalTwins.add(computeEternalTwinsPartition(ls, line));
        eternalTwins.add(computeEternalTwinsMEI(ls, line));
        eternalTwins.add(computeEternalTwinsMLEI(ls, line));
        System.out.println("Computation done ");
//        deltaTwins = computeEternalTwinsNaively(ls, line);
//        deltaTwins = computeDeltaTwinsNaively(ls, line, delta);
//        deltaTwins = computeDeltaTwinsMEI(ls, line, delta);
//        deltaTwins = computeDeltaTwinsMLEI(ls, line, delta);

        // Assert that all computations have the same results
        assert (eternalTwins.stream().allMatch(t -> eternalTwins.get(0).size() == t.size()));
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

    public static List<DeltaEdge> computeEternalTwinsPartition(LinkStream ls, List<String> line) {
        System.out.println("COMPUTING ETERNAL TWINS PARTITION");
        ArrayList<DeltaEdge> eternalTwins = new ArrayList<>();
        try {
            Date startTime = new Date();
            eternalTwins = TwinAlgorithms.computeEternalTwinsPartition(ls);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line.add(String.valueOf(timeElapsed));
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line.add("OUT OF MEMORY");
        }
        System.out.println("We have " + eternalTwins.size() + " eternal twins");
        return eternalTwins;
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

    public static List<DeltaEdge> computeEternalTwinsMEI(LinkStream ls, List<String> line) {
        System.out.println("COMPUTING ETERNAL TWINS USING EDGES ITERATION");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.computeEternalTwinsByEdgesIteration(ls);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line.add(String.valueOf(timeElapsed));
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line.add("OUT OF MEMORY,");
        }
        System.out.println("We have " + deltaTwins.size() + " eternal twins");
        return deltaTwins;
    }

    public static List<DeltaEdge> computeEternalTwinsMLEI(LinkStream ls, List<String> line) {
        System.out.println("COMPUTING ETERNAL TWINS USING MATRIX LESS EDGES ITERATION");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.computeEternalTwinsByEdgesIterationWithoutMatrices(ls);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line.add(String.valueOf(timeElapsed));
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line.add("OUT OF MEMORY,");
        }
        System.out.println("We have " + deltaTwins.size() + " eternal twins");
        return deltaTwins;
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
