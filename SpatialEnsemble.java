import java.util.LinkedList;
import java.util.List;

public class SpatialEnsemble {
    int patchNumber;
    int x_size, y_size;
    Utility.Direction[] dir;

    float patchDistance(Patch f, Patch s) {
        float res = 0.f;
        for (Utility.Element fel : f.els)
            for (Utility.Element sel : s.els)
                res += Utility.distance(fel.features, sel.features);
        res /= f.els.size() * s.els.size();
        return res;
    }

    class PairEntry {
        boolean isfirst;
        PatchPair p;

        PairEntry (PatchPair p, boolean isfirst) {
            this.isfirst = isfirst;
            this.p = p;
        }
    }

    class PatchPair {
        Patch f, s;
        float distance;

        PatchPair(Patch f, Patch s) {
            this.f = f;
            this.s = s;
            distance = patchDistance(f, s);
        }
    }

    class Patch {
        int label;
        List<Utility.Element> els;
        List<PairEntry> entries;

        Patch (Utility.Element el) {
            this.els = new LinkedList<Utility.Element>();
            els.add(el);
            this.label = el.cls;
        }

        void merge(Patch p) {
            if (label == -1)
                label = p.label;
            els.addAll(p.els);

        }
    }

    SpatialEnsemble (int patchNumber) {
        this.patchNumber = patchNumber;
        this.dir = new Utility.Direction[4];
        dir[0] = new Utility.Top(x_size, y_size);
        dir[1] = new Utility.Bottom(x_size, y_size);
        dir[2] = new Utility.Left(x_size, y_size);
        dir[3] = new Utility.Right(x_size, y_size);
    }

    void HomogenPatchGen (Utility.Element[] data) {
        Patch[] patches = new Patch[data.length];
        for (int i = 0; i < patches.length; ++i)
            patches[i] = new Patch(data[i]);

        List<PatchPair> ListPair = new LinkedList<PatchPair>();

        for (int i = 0; i < patches.length; ++i)
            for (int j = 0; j < 4; ++j) {
                int pos = dir[j].get(patches[i].els.get(0).pos);
                if (pos == -1)
                    continue;
                if (pos > i) {
                    ListPair.add(new PatchPair(patches[i], patches[pos]));
                    patches[i].entries.add(new PairEntry(ListPair.get(ListPair.size() - 1), true));
                    patches[pos].entries.add(new PairEntry(ListPair.get(ListPair.size() - 1), false));
                }
            }

        int closestPair = -1;
        int patchCount = patches.length;
        float minDistance = Float.POSITIVE_INFINITY;
        while(patchCount > patchNumber) {
            for (int i = 0; i < ListPair.size(); ++i) {
                PatchPair p = ListPair.get(i);
                if (p.f.label == p.s.label || p.f.label == -1 || p.s.label == -1)
                    if (Float.compare(p.distance, minDistance) < 0) {
                        minDistance = p.distance;
                        closestPair = i;
                    }
            }
            PatchPair p = ListPair.get(closestPair);
            p.f.merge(p.s);
            --patchCount;
        }
    }

}
