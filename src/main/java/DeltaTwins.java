import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DeltaTwins {

    //CHANGE DIRECTORY PATH AND VALUE OF GAMMA HERE
    private static String directory = "data/timeprogressionexcerpt";
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
        List<String> header = new ArrayList<String>(
                Arrays.asList(
                        "Dataset",
                        "Number of vertices",
                        "Number of edges",
                        "Number of time instants"
                )
        );
        List<List<String>> results = new ArrayList<>();
        results.add(header);

        for (String filepath : dataSets) {
            System.out.println("COMPUTING FOR " + filepath);

            LinkStream ls = initiate(filepath);
            String vertices = String.valueOf(ls.getVertices().size());
            String edges = String.valueOf(ls.getLinks().size());
            String instants = String.valueOf(ls.getEndInstant() - ls.getStartInstant());
            System.out.println(
                    "Number of vertices : " + vertices
                            + ", Number of edges : " + edges
                            + ", for " + instants + " instants"
            );
            // Get computed headers and results here
            DeltaTwins.compute(ls);


            // Create line of result
            List<String> line = new ArrayList<>(
                    Arrays.asList(
                            filepath,
                            vertices,
                            edges,
                            instants
                    )
            );
            results.add(line);
        }
        try {
            ResultBuilder resultBuilder = new ResultBuilder();
            String filename = "result-" + f.getName();
            resultBuilder.writeResult(results, filename);
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Could not write result file");
        }

    }

    public static ArrayList<String> compute(LinkStream ls) {
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        ArrayList<String> output = new ArrayList<>();
        String line = new String();
//        deltaTwins = computeEternalTwinsNaively(ls, line);
//        deltaTwins = computeEternalTwinsMEI(ls, line);
        deltaTwins = computeEternalTwinsMLEI(ls, line);
//        deltaTwins = computeDeltaTwinsNaively(ls, line, delta);
//        deltaTwins = computeDeltaTwinsMEI(ls, line, delta);
//        deltaTwins = computeDeltaTwinsMLEI(ls, line, delta);
        output.add(line);
        System.out.println("Computation done ");
        return output;
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

    public static ArrayList<DeltaEdge> computeEternalTwinsMLEI(LinkStream ls, String line) {

        System.out.println("COMPUTING ETERNAL TWINS USING EDGES ITERATION WITHOUT MATRICES");
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();

        try {
            Date startTime = new Date();
            deltaTwins = TwinAlgorithms.computeEternalTwinsByEdgesIterationWithoutMatrices(ls);
            Date endTime = new Date();
            long timeElapsed = endTime.getTime() - startTime.getTime();
            line = line.concat(timeElapsed + ",,");
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory");
            line = line.concat("OUT OF MEMORY,,");
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
