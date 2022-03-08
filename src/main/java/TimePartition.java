import java.util.ArrayList;

public class TimePartition {

    private int allTimeStartInstant;
    private int allTimeEndInstant;
    private int startInstant;
    private int endInstant;
    private TimePartition partBefore;
    private TimePartition partAfter;
    private int depth;

    public TimePartition(int startInstant, int endInstant, int allTimeStartInstant, int allTimeEndInstant) {
        this.startInstant = startInstant;
        this.endInstant = endInstant;
        this.allTimeStartInstant = allTimeStartInstant;
        this.allTimeEndInstant = allTimeEndInstant;
        this.depth = 1;
    }

    public int getAllTimeStartInstant() {
        return this.allTimeStartInstant;
    }

    public int getAllTimeEndInstant() {
        return this.allTimeEndInstant;
    }

    public int getStartInstant() {
        return this.startInstant;
    }

    public int getEndInstant() {
        return this.endInstant;
    }

    public TimePartition getPartBefore() {
        return this.partBefore;
    }

    public TimePartition getPartAfter() {
        return this.partAfter;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setAllTimeStartInstant(int allTimeStartInstant) {
        this.allTimeStartInstant = allTimeStartInstant;
    }

    public void setAllTimeEndInstant(int allTimeEndInstant) {
        this.allTimeEndInstant = allTimeEndInstant;
    }

    public void setStartInstant(int startInstant) {
        this.startInstant = startInstant;
    }

    public void setEndInstant(int endInstant) {
        this.endInstant = endInstant;
    }

    public void setPartBefore(TimePartition partBefore) {
        this.partBefore = partBefore;
    }

    public void setPartAfter(TimePartition partAfter) {
        this.partAfter = partAfter;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void removeInstant(int t, int delta) {

        if (t == startInstant - 1) {
            this.startInstant = t;
            if (this.partBefore != null) {
                this.partBefore.recursivelyChangeEndInstant(t);
            }
        }

        if (t == endInstant + 1) {
            this.endInstant = t;
            if (this.partAfter != null) {
                this.partAfter.recursivelyChangeStartInstant(t);
            }
        }

        if (t < startInstant - 1) {
            if (this.partBefore == null) {
                partBefore = new TimePartition(t, t, allTimeStartInstant, startInstant - 1);
            } else {
                partBefore.removeInstant(t, delta);
            }
            this.setDepth(Math.max(this.getDepth(), this.partBefore.getDepth() + 1));
        }

        if (t > endInstant + 1) {
            if (this.partAfter == null) {
                partAfter = new TimePartition(t, t, endInstant + 1, allTimeEndInstant);
            } else {
                partAfter.removeInstant(t, delta);
            }
            this.setDepth(Math.max(this.getDepth(), this.partAfter.getDepth() + 1));
        }

        if (startInstant - allTimeStartInstant < delta) {
            this.startInstant = this.allTimeStartInstant;
            this.partBefore = null;
            if (this.partAfter != null) {
                this.setDepth(this.partAfter.getDepth() + 1);
            } else {
                this.setDepth(1);
            }
        }

        if (allTimeEndInstant - endInstant < delta) {
            this.endInstant = this.allTimeEndInstant;
            this.partAfter = null;
            if (this.partBefore != null) {
                this.setDepth(this.partBefore.getDepth() + 1);
            } else {
                this.setDepth(1);
            }
        }
        if ((this.partBefore != null) && (this.partBefore.getEndInstant() + 1 == this.startInstant)) {
            this.mergeWithSonLeft();
        }
        if ((this.partAfter != null) && (this.partAfter.getStartInstant() - 1 == this.endInstant)) {
            this.mergeWithSonRight();
        }


      //      this.balanceThatTree();


    }

    public void balanceThatTree() {
        int depthLeft = 0;
        int depthRight = 0;

        if (this.partBefore != null) {
            depthLeft = this.partBefore.getDepth();
        }

        if (this.partAfter != null) {
            depthRight = this.partAfter.getDepth();
        }

        if (depthLeft > depthRight + 1) {
            rotateRight();
        } else if (depthLeft + 1 < depthRight) {
            rotateLeft();
        }
    }

    public void rotateRight() {
        TimePartition oldRoot = new TimePartition(this.startInstant, this.endInstant, this.partBefore.getEndInstant() +1, this.allTimeEndInstant);
        oldRoot.setPartAfter(this.partAfter);;

        TimePartition newRoot = new TimePartition(this.partBefore.getStartInstant(), this.partBefore.getEndInstant(), this.partBefore.getAllTimeStartInstant(), this.getAllTimeEndInstant());
        newRoot.setPartBefore(this.partBefore.getPartBefore());

        if (this.partBefore.getPartAfter() != null) {
            oldRoot.setPartBefore(this.partBefore.getPartAfter());
        }
        newRoot.setPartAfter(oldRoot);

        oldRoot.setDepth(1);
        if (oldRoot.getPartBefore() != null) {
            oldRoot.setDepth(Math.max(oldRoot.getDepth(), oldRoot.getPartBefore().getDepth() + 1));
        }

        if (oldRoot.getPartAfter() != null) {
            oldRoot.setDepth(Math.max(oldRoot.getDepth(), oldRoot.getPartAfter().getDepth() + 1));
        }

        newRoot.setDepth(Math.max(1, oldRoot.getDepth() + 1));

        if (newRoot.getPartBefore() != null) {
            newRoot.setDepth(Math.max(newRoot.getDepth(), newRoot.getPartBefore().getDepth() + 1));
        }

        this.setPartAfter(newRoot.getPartAfter());
        this.setPartBefore(newRoot.getPartBefore());
        this.setAllTimeEndInstant(newRoot.getAllTimeEndInstant());
        this.setAllTimeStartInstant(newRoot.getAllTimeStartInstant());
        this.setEndInstant(newRoot.getEndInstant());
        this.setStartInstant(newRoot.getStartInstant());
        this.setDepth(newRoot.getDepth());
    }

    public void rotateLeft() {
        TimePartition oldRoot = new TimePartition(this.startInstant, this.endInstant, this.allTimeStartInstant, this.partBefore.getStartInstant() - 1);
        oldRoot.setPartBefore(this.partBefore);;

        TimePartition newRoot = new TimePartition(this.partAfter.getStartInstant(), this.partAfter.getEndInstant(), this.partAfter.getAllTimeStartInstant(), this.partAfter.getAllTimeStartInstant());
        newRoot.setPartAfter(this.partAfter.getPartAfter());

        if (this.partAfter.getPartBefore() != null) {
            oldRoot.setPartAfter(this.partAfter.getPartBefore());
        }
        newRoot.setPartBefore(oldRoot);

        oldRoot.setDepth(1);
        if (oldRoot.getPartBefore() != null) {
            oldRoot.setDepth(Math.max(oldRoot.getDepth(), oldRoot.getPartBefore().getDepth() + 1));
        }

        if (oldRoot.getPartAfter() != null) {
            oldRoot.setDepth(Math.max(oldRoot.getDepth(), oldRoot.getPartAfter().getDepth() + 1));
        }

        newRoot.setDepth(Math.max(1, oldRoot.getDepth() + 1));

        if (newRoot.getPartAfter() != null) {
            newRoot.setDepth(Math.max(newRoot.getDepth(), newRoot.getPartAfter().getDepth() + 1));
        }

        this.setPartAfter(newRoot.getPartAfter());
        this.setPartBefore(newRoot.getPartBefore());
        this.setAllTimeEndInstant(newRoot.getAllTimeEndInstant());
        this.setAllTimeStartInstant(newRoot.getAllTimeStartInstant());
        this.setEndInstant(newRoot.getEndInstant());
        this.setStartInstant(newRoot.getStartInstant());
        this.setDepth(newRoot.getDepth());
    }

    public void mergeWithSonLeft() {
        this.startInstant = partBefore.getStartInstant();
        TimePartition tmp;
        if (this.partBefore.getPartBefore() == null) {
            tmp = null;
        }
        else {
            tmp = this.partBefore.getPartBefore();
        }
        this.partBefore = tmp;
        this.setDepth(this.depth - 1);
    }

    public void mergeWithSonRight() {
        this.endInstant = partAfter.getEndInstant();
        TimePartition tmp;
        if (this.partAfter.getPartAfter() == null) {
            tmp = null;
        }
        else {
            tmp = this.partAfter.getPartAfter();
        }
        this.partAfter = tmp;
        this.setDepth(this.depth - 1);

    }

    public ArrayList<DeltaEdge> getAllDeltaIntervals(int delta) {
        ArrayList<DeltaEdge> response = new ArrayList<>();
        if (partBefore == null) {
            if (this.startInstant - this.allTimeStartInstant >= delta) {
                DeltaEdge e = new DeltaEdge(this.allTimeStartInstant, this.startInstant - 1, null, null);
                response.add(e);
            }
        } else {
            response.addAll(this.partBefore.getAllDeltaIntervals(delta));
        }

        if (partAfter == null) {
            if (this.allTimeEndInstant - this.endInstant >= delta) {
                DeltaEdge e = new DeltaEdge(this.endInstant + 1, this.allTimeEndInstant, null, null);
                response.add(e);
            }
        } else {
            response.addAll(this.partAfter.getAllDeltaIntervals(delta));
        }

        return response;
    }

    public void recursivelyChangeStartInstant(int t) {
        if (this.partBefore != null) {
            this.partBefore.recursivelyChangeStartInstant(t);
        }
        this.allTimeStartInstant = t;
    }

    public void recursivelyChangeEndInstant(int t) {
        if (this.partBefore != null) {
            this.partBefore.recursivelyChangeEndInstant(t);
        }
        this.allTimeEndInstant = t;
    }
}
