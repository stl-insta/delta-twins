import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class TwinAlgorithms {


    public static ArrayList<DeltaEdge> naivelyComputeEternalTwins(LinkStream ls) {
        Date startTime = new Date();
        ArrayList<DeltaEdge> eternalTwins = new ArrayList<>();

        for (int i = 0; i <= ls.getEndInstant(); i++) {
            eternalTwins = naivelyComputeDeltaTwinsForInstant(ls, i, i, eternalTwins);
        }
        ArrayList<DeltaEdge> result = new ArrayList<>();

        for (DeltaEdge e : eternalTwins) {
            if (e.isDeltaEdge(ls.getEndInstant())) {
                result.add(e);
            }
        }
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time elapsed : " + timeElapsed);
        return result;
    }

    public static ArrayList<DeltaEdge> computeEternalTwinsByEdgesIteration(LinkStream ls) {
        Date startTime = new Date();

        ls.computeMatrixSuite();
        int[][] twinCandidates = new int[ls.getVertices().size()][ls.getVertices().size()];
        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = 0; j < ls.getVertices().size(); j++) {
                twinCandidates[i][j] = 1;
            }
        }

        for (TemporalEdge e : ls.getLinks()) {
            for (Vertex z : ls.getVertices()) {
                if ((!z.equals(e.getU())) && (!z.equals(e.getV()))) {
                    if (twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] == 1) {
                        if (ls.getMatrixSuite().get(e.getT() - ls.getStartInstant())[ls.getVertices().indexOf(z)][ls
                                .getVertices().indexOf(e.getV())] == 0) {
                            twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] = 0;
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())] = 0;
                        }
                    }
                    if (twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] == 1) {
                        if (ls.getMatrixSuite().get(e.getT() - ls.getStartInstant())[ls.getVertices().indexOf(z)][ls
                                .getVertices().indexOf(e.getU())] == 0) {
                            twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] = 0;
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())] = 0;
                        }
                    }
                }
            }
        }

        ArrayList<DeltaEdge> twins = new ArrayList<>();

        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = i + 1; j < ls.getVertices().size(); j++) {
                if (twinCandidates[i][j] == 1) {
                    DeltaEdge e = new DeltaEdge(ls.getStartInstant(), ls.getEndInstant(), ls.getVertices().get(i), ls
                            .getVertices().get(j));
                    twins.add(e);
                }
            }
        }
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time elapsed : " + timeElapsed);
        return twins;
    }

    public static ArrayList<DeltaEdge> computeEternalTwinsByEdgesIterationWithoutMatrices(LinkStream ls) {
        Date startTime = new Date();

        int[][] twinCandidates = new int[ls.getVertices().size()][ls.getVertices().size()];
        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = 0; j < ls.getVertices().size(); j++) {
                twinCandidates[i][j] = 1;
            }
        }

        HashMap<Integer, ArrayList<TemporalEdge>> mapEdgeByInstant = new HashMap<>();
        for (TemporalEdge e : ls.getLinks()) {
            if (!mapEdgeByInstant.containsKey(e.getT())) {
                ArrayList<TemporalEdge> newList = new ArrayList<>();
                newList.add(e);
                mapEdgeByInstant.put(e.getT(), newList);
            }
            else {
                mapEdgeByInstant.get(e.getT()).add(e);
            }
        }


        for (TemporalEdge e : ls.getLinks()) {
            for (Vertex z : ls.getVertices()) {
                if ((!z.equals(e.getU())) && (!z.equals(e.getV()))) {
                    boolean linkUexists = false;
                    boolean linkVexists = false;
                    for (TemporalEdge f : mapEdgeByInstant.get(e.getT())) {
                        if ((twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())] == 1) && (((f.getU().equals(z)) && (f.getV().equals(e.getU()))) ||((f.getU().equals(e.getU())) && (f.getV().equals(z))))) {
                            linkUexists = true;
                        }
                        if ((twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())] == 1) && (((f.getU().equals(z)) && (f.getV().equals(e.getV()))) ||((f.getU().equals(e.getV())) && (f.getV().equals(z))))) {
                            linkVexists = true;
                        }
                        if (linkUexists && linkVexists) {
                            break;
                        }

                    }

                    if (!linkVexists) {
                        twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] = 0;
                        twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())] = 0;

                    }
                    if (!linkUexists) {
                        twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] = 0;
                        twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())] = 0;
                    }

                }
            }
        }


        ArrayList<DeltaEdge> twins = new ArrayList<>();

        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = i + 1; j < ls.getVertices().size(); j++) {
                if (twinCandidates[i][j] == 1) {
                    DeltaEdge e = new DeltaEdge(ls.getStartInstant(), ls.getEndInstant(), ls.getVertices().get(i), ls
                            .getVertices().get(j));
                    twins.add(e);
                }
            }
        }
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time elapsed : " + timeElapsed);
        return twins;
    }

    public static ArrayList<DeltaEdge> naivelyComputeDeltaTwins(LinkStream ls, int delta) {
        Date startTime = new Date();
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        ArrayList<DeltaEdge> twinsAtT = new ArrayList<>();
        for (int t = ls.getStartInstant(); t <= ls.getEndInstant(); t++) {
            twinsAtT = naivelyComputeDeltaTwinsForInstant(ls, t, delta, twinsAtT);
            for (DeltaEdge e : twinsAtT) {
                if (e.isDeltaEdge(delta)) {
                    boolean alreadythere = false;
                    for (DeltaEdge g : deltaTwins) {
                        if (((g.getU().equals(e.getU()) && g.getV().equals(e.getV())) || (g.getV().equals(e.getU())
                                && g.getU().equals(e.getV()))) && (g.getStartInstant() == e.getStartInstant())) {
                            g.setEndInstant(t);
                            alreadythere = true;
                        }
                    }
                    if (!alreadythere) {
                        deltaTwins.add(e);
                    }
                }
            }
        }
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time elapsed : " + timeElapsed);
        return deltaTwins;
    }
    
    public static ArrayList<DeltaEdge> computeDeltaTwinsByEdgesIteration(LinkStream ls, int delta) {
        Date startTime = new Date();

        ls.computeMatrixSuite();
        TimePartition[][] twinCandidates = new TimePartition[ls.getVertices().size()][ls.getVertices().size()];

        for (TemporalEdge e : ls.getLinks()) {
            for (Vertex z : ls.getVertices()) {
                if ((!z.equals(e.getU())) && (!z.equals(e.getV()))) {
                        if (ls.getMatrixSuite().get(e.getT() - ls.getStartInstant())[ls.getVertices().indexOf(z)][ls
                                .getVertices().indexOf(e.getV())] == 0) {
                            if (twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] == null) {
                                twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                                twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                            }
                            else {
                                twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)].removeInstant(e.getT(), delta);
                                twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())].removeInstant(e.getT(), delta);
                            }
                        }

                    if (ls.getMatrixSuite().get(e.getT() - ls.getStartInstant())[ls.getVertices().indexOf(z)][ls
                            .getVertices().indexOf(e.getU())] == 0) {
                        if (twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] == null) {
                            twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                        }
                        else {
                            twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)].removeInstant(e.getT(), delta);
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())].removeInstant(e.getT(), delta);
                        }
                    }
                }
            }
        }

         ArrayList<DeltaEdge> twins = new ArrayList<>();

        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = i + 1; j < ls.getVertices().size(); j++) {
                if (twinCandidates[i][j] == null) {
                    if (ls.getEndInstant() - ls.getStartInstant() >= delta) {
                        DeltaEdge e = new DeltaEdge(ls.getStartInstant(), ls.getEndInstant(), ls.getVertices().get(i), ls.getVertices().get(j));

                        twins.add(e);
                    }
                }
                else {
                    if (!twinCandidates[i][j].getAllDeltaIntervals(delta).isEmpty()) {
                        for (DeltaEdge e : twinCandidates[i][j].getAllDeltaIntervals(delta)) {
                            e.setU(ls.getVertices().get(i));
                            e.setV(ls.getVertices().get(j));
                            twins.add(e);
                        }
                    }
                }
            }
        }
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time elapsed : " + timeElapsed);
        return twins;
    }

    public static ArrayList<DeltaEdge> computeDeltaTwinsByEdgesIterationWithoutMatrices(LinkStream ls, int delta) {
        Date startTime = new Date();

        TimePartition[][] twinCandidates = new TimePartition[ls.getVertices().size()][ls.getVertices().size()];
        HashMap<Integer, ArrayList<TemporalEdge>> mapEdgeByInstant = new HashMap<>();
        for (TemporalEdge e : ls.getLinks()) {
            if (!mapEdgeByInstant.containsKey(e.getT())) {
                ArrayList<TemporalEdge> newList = new ArrayList<>();
                newList.add(e);
                mapEdgeByInstant.put(e.getT(), newList);
            }
            else {
                mapEdgeByInstant.get(e.getT()).add(e);
            }
        }


        for (TemporalEdge e : ls.getLinks()) {
            for (Vertex z : ls.getVertices()) {
                if ((!z.equals(e.getU())) && (!z.equals(e.getV()))) {
                    boolean linkUexists = false;
                    boolean linkVexists = false;
                    for (TemporalEdge f : mapEdgeByInstant.get(e.getT())) {
                            if (((f.getU().equals(z)) && (f.getV().equals(e.getU()))) ||((f.getU().equals(e.getU())) && (f.getV().equals(z)))) {
                                linkUexists = true;
                            }
                            if (((f.getU().equals(z)) && (f.getV().equals(e.getV()))) ||((f.getU().equals(e.getV())) && (f.getV().equals(z)))) {
                                linkVexists = true;
                            }
                            if (linkUexists && linkVexists) {
                                break;
                            }

                    }

                    if (!linkVexists) {
                        if (twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] == null) {
                            twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                        }
                        else {
                            twinCandidates[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(z)].removeInstant(e.getT(), delta);
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getU())].removeInstant(e.getT(), delta);
                        }
                    }
                    if (!linkUexists) {
                        if (twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] == null) {
                            twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())] = new TimePartition(e.getT(), e.getT(), ls.getStartInstant(), ls.getEndInstant());
                        }
                        else {
                            twinCandidates[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(z)].removeInstant(e.getT(), delta);
                            twinCandidates[ls.getVertices().indexOf(z)][ls.getVertices().indexOf(e.getV())].removeInstant(e.getT(), delta);
                        }
                    }

                }
            }
        }

        ArrayList<DeltaEdge> twins = new ArrayList<>();

        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = i + 1; j < ls.getVertices().size(); j++) {
                if (twinCandidates[i][j] == null) {
                    if (ls.getEndInstant() - ls.getStartInstant() >= delta) {
                        DeltaEdge e = new DeltaEdge(ls.getStartInstant(), ls.getEndInstant(), ls.getVertices().get(i), ls.getVertices().get(j));

                        twins.add(e);
                    }
                }
                else {
                    if (!twinCandidates[i][j].getAllDeltaIntervals(delta).isEmpty()) {
                        for (DeltaEdge e : twinCandidates[i][j].getAllDeltaIntervals(delta)) {
                            e.setU(ls.getVertices().get(i));
                            e.setV(ls.getVertices().get(j));
                            twins.add(e);
                        }
                    }
                }
            }
        }
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time elapsed : " + timeElapsed);
        return twins;
    }

    public static ArrayList<DeltaEdge> naivelyComputeDeltaTwinsForInstant(LinkStream ls, int t, int delta, ArrayList<DeltaEdge>
            deltaTwinsLastInstant) {
        ArrayList<DeltaEdge> deltaTwins = new ArrayList<>();
        ArrayList<TemporalEdge> twinsNow = naivelyComputeTwinsProperly(ls, t);

        for (TemporalEdge e : twinsNow) {
            boolean alreadythere = false;
            for (DeltaEdge g : deltaTwinsLastInstant) {
                if ((e.getU() == g.getU() && e.getV() == g.getV()) || (e.getU() == g.getV() && e.getV() == g.getU())) {
                    g.setEndInstant(t);
                    deltaTwins.add(g);
                    alreadythere = true;
                    break;
                }
            }
            if (!alreadythere) {
                DeltaEdge g = new DeltaEdge(t, t, e.getU(), e.getV());
                deltaTwins.add(g);
            }
        }
        return deltaTwins;
    }

    public static ArrayList<TemporalEdge> naivelyComputeTwinsProperly(LinkStream ls, int t) {
        ArrayList<TemporalEdge> twins = new ArrayList<>();

        int[][] m = new int[ls.getVertices().size()][ls.getVertices().size()];
        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = 0; j < ls.getVertices().size(); j++) {
                m[i][j] = 0;
            }
        }

        for (TemporalEdge e : ls.getLinks()) {
            if (e.getT() == t) {
                m[ls.getVertices().indexOf(e.getU())][ls.getVertices().indexOf(e.getV())] = 1;
                m[ls.getVertices().indexOf(e.getV())][ls.getVertices().indexOf(e.getU())] = 1;
            }
        }
        for (int i = 0; i < ls.getVertices().size(); i++) {
            for (int j = i + 1; j < ls.getVertices().size(); j++) {
                if (i != j) {
                    int twin = 1;
                    for (int k = 0; k < ls.getVertices().size(); k++) {
                        if ((k != i) && (k != j) && (m[i][k] != m[j][k])) {
                            twin = 0;
                            break;
                        }
                    }
                    if (twin == 1) {
                        TemporalEdge e = new TemporalEdge(t, ls.getVertices().get(i), ls.getVertices().get(j));
                        twins.add(e);
                    }
                }

            }
        }
        return twins;
    }
}
