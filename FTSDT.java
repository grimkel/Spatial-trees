import jdk.jshell.spi.ExecutionControl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class FTSDT extends Tree{
    int max_s, min_node_size;
    int x_size, y_size;
    Integer[] foc_val;
    boolean adaptiveNeigh;
    Boolean[] ind;

    sNode root;

    //--------------------------------------
    Integer[] testY;

    void show_Y () {
        Utility.<Integer>print_vec(x_size, y_size, testY);
    }

    static class sNode extends Utility.Node {
        int s;

        sNode l;
        sNode r;
    }

    class Bounds {
        int lBound, rBound, botBound, topBound, area;

        Bounds (int indx, int s) {
            int
                    pos_x = indx % x_size,
                    pos_y = indx / y_size;

            lBound = Math.max(0, pos_x - s);
            rBound = Math.min(x_size - 1, pos_x + s);
            topBound = Math.max(0, pos_y - s);
            botBound = Math.min(y_size - 1, pos_y + s);
            area = (rBound - lBound + 1) * (botBound - topBound + 1);
        }
    }

    class Split {
        int amntL, amntR;
        int[] cntL, cntR;
        boolean[] split;

        HashMap<Integer, Integer> pos2id;

        Split (int size) {
            pos2id = new HashMap<Integer, Integer>();
            split = new boolean[size];
            cntL = new int[_n_classes];
            cntR = new int[_n_classes];
            amntL = 0;
            amntR = 0;
        }

        void setVal (int pos, int id, boolean val, int cls) {
            pos2id.put(pos, id);
            split[id] = val;

            if (val) {
                ++cntL[cls];
                ++amntL;
            } else {
                ++cntR[cls];
                ++amntR;
            }
        }

        void updateVal(int pos, boolean val, int cls) {
            int id = pos2id.get(pos);
            if (split[id] == val)
                return;

            split[id] = val;
            if (val) {
                --cntL[cls];
                ++cntR[cls];
            } else {
                --cntR[cls];
                ++cntL[cls];
            }
        }

        boolean getSide(int pos) { return split[pos2id.get(pos)]; }
    }


    public FTSDT(int max_depth, int min_node_size, int max_s, boolean adaptiveNeigh) {
        super(max_depth, min_node_size);
        this.max_s = max_s;
        this.min_node_size = min_node_size;
        this.adaptiveNeigh = adaptiveNeigh;

        root = new sNode();
    }

    public void fit(float[][][] X, int[][] Y) {
        this.x_size = X[0].length;
        this.y_size = X.length;

        data = prepare_data(X, Y);

        testY = new Integer[data.length];

        ind = new Boolean[data.length];
        foc_val = new Integer[data.length];

        _best_split(root, 1, data.clone());
    }


    float focal_func(int s, int cur, boolean adaptiveNeigh) {

        Bounds bounds = new Bounds(cur, s);

        int val = 0, tmp;
        if (adaptiveNeigh) {


        } else {
            for (int x = bounds.lBound; x <= bounds.rBound; ++x)
                for (int y = bounds.topBound; y <= bounds.botBound; ++y) {
                    tmp = 1;
                    if (!ind[x + y * x_size])
                        tmp *= -1;
                    if (!ind[cur])
                        tmp *= -1;
                    val += tmp;
                }
        }

        foc_val[cur] = val;
        return (float) val / bounds.area;
    }

    Split nodeSplit (Utility.Element[] data, int s, float thr, int feature_id, boolean adaptiveNeigh) {

        int tmp;
        for (Utility.Element el : this.data) {
            tmp = Float.compare(thr, el.features[feature_id]);
            ind[el.pos] = tmp >= 0;
        }

        Split split = new Split(data.length);

        for (int i = 0; i < data.length; ++i) {
            float foc = focal_func(s, data[i].pos, adaptiveNeigh);
            boolean foc_test = ind[data[i].pos] ^ (Float.compare(foc, 0.f) < 0);
            split.setVal(data[i].pos, i, foc_test, data[i].cls);
        }

        return split;
    }

    void nodeSplitUpdate (Utility.Element[] data, int s, boolean adaptiveNeigh, int cur, Split split, HashSet<Integer> pos_set) {
        if (ind[data[cur].pos])
            return;
        ind[data[cur].pos] = true;
        Bounds bounds = new Bounds(data[cur].pos, s);

        foc_val[data[cur].pos] = -Integer.signum(foc_val[data[cur].pos]) * (Math.abs(foc_val[data[cur].pos]) - 1) + 1;
        int pos, tmp;
        for (int x = bounds.lBound; x <= bounds.rBound; ++x)
            for (int y = bounds.topBound; y <= bounds.botBound; ++y) {
                pos = x + y * x_size;

                if (pos == data[cur].pos)
                    continue;

                tmp = 1;
                if (!ind[pos])
                    tmp *= -1;
                if (!ind[data[cur].pos])
                    tmp *= -1;

                foc_val[pos] += 2 * tmp;

                if (pos_set.contains(pos)) {
                    Bounds pos_bounds = new Bounds(pos, s);
                    float foc = (float) foc_val[pos] / pos_bounds.area;
                    boolean foc_test = ind[pos] ^ (Float.compare(foc, 0.f) < 0);

                    split.updateVal(pos, foc_test, this.data[pos].cls);
                }
            }
    }

    void _best_split(sNode node, int depth, Utility.Element[] data) {
        System.out.println(
                "##################################\n" +
                "##################################\n");
        Character[] map_check = new Character[this.data.length];
        Arrays.fill(map_check, 'Z');
        for (Utility.Element el : data)
            map_check[el.pos] = '*';

        Utility.<Character>print_vec(x_size, y_size, map_check);

        int[] cnt = prepare_node(node, depth, data);
        System.out.println(node.label);
        if (node.isleaf) {
            for (Utility.Element el : data)
                testY[el.pos] = node.label;
            return;
        }

        HashSet<Integer> pos_set = new HashSet<Integer>();
        for (Utility.Element el : data)
            pos_set.add(el.pos);

        float best_ig = Float.NEGATIVE_INFINITY, base_entropy = Utility.get_entropy(cnt, data.length);
        System.out.printf("Base entropy: %f\n", base_entropy);
        for (int feature_id = 0; feature_id < data[0].features.length; ++feature_id) {

            Arrays.sort(data, new Utility.SortByFeature(feature_id));
            for (int s = 1; s <= max_s; ++s) {

                Split split = new Split(data.length);

                int i0 = 0;
                for (; i0 < data.length; ++i0)
                    if (Float.compare(data[i0].features[feature_id], data[min_node_size].features[feature_id]) == 1)
                        break;
                for (int i = i0 - 1; i < data.length - min_node_size; ++i) {
                    if (i == i0 - 1)
                        split = nodeSplit(data, s, data[i].features[feature_id], feature_id, adaptiveNeigh);
                    else
                        nodeSplitUpdate(data, s, adaptiveNeigh, i, split, pos_set);


                    if (Float.compare(data[i].features[feature_id], data[i + 1].features[feature_id]) == 0)
                        continue;

                    float
                            entropy_l = Utility.get_entropy(split.cntL, split.amntL),
                            entropy_r = Utility.get_entropy(split.cntR, split.amntR);


                    float ig = base_entropy - (entropy_l * split.amntL+ entropy_r * split.amntR) / data.length;
                    if (Float.compare(ig, best_ig) == 1) {
                        best_ig = ig;
                        System.out.printf("###################( NEW BEST: f - %f, s - %d entropy_l - %f, entropy_r - %f, ig - %f)##################\n", data[i].features[feature_id], s, entropy_l, entropy_r, ig);
                        Integer[] tmp = new Integer[x_size * y_size];
                        Arrays.fill(tmp, 0);
                        for (Utility.Element el : data)
                            if (split.getSide(el.pos))
                                tmp[el.pos] = 1;
                            else
                                tmp[el.pos] = -1;
                        Utility.print_vec(x_size, y_size, tmp);
                        node.s = s;
                        node.feature_id = feature_id;
                        node.test_val = data[i].features[feature_id];
                    }
                }
            }
        }

        Split split = nodeSplit(data, node.s, node.test_val, node.feature_id, adaptiveNeigh);
        Utility.Element[]
                l_els = new Utility.Element[split.amntL],
                r_els = new Utility.Element[split.amntR];
        int l_indx = 0, r_indx = 0;
        for (Utility.Element el : data)
            if (split.getSide(el.pos))
                l_els[l_indx++] = el;
            else
                r_els[r_indx++] = el;

        if (l_indx == 0 || r_indx == 0) {
            node.isleaf = true;
            for (Utility.Element el : data)
                testY[el.pos] = node.label;
            return;
        }

        node.l = new sNode();
        node.r = new sNode();

        _best_split(node.l, depth + 1, l_els);
        _best_split(node.r, depth + 1, r_els);
    }

    Utility.Element[] _predict (sNode node, Utility.Element[] X) {
        if (node.isleaf) {
            for (Utility.Element x : X)
                x.cls = node.label;
            return X;
        }

        Split split = nodeSplit(X, node.s, node.test_val, node.feature_id, adaptiveNeigh);
        Utility.Element[]
                l_els = new Utility.Element[split.amntL],
                r_els = new Utility.Element[split.amntR];
        int l_indx = 0, r_indx = 0;
        for (Utility.Element el : X)
            if (split.getSide(el.pos))
                l_els[l_indx++] = el;
            else
                r_els[r_indx++] = el;

        Utility.Element[] Y = new Utility.Element[X.length];
        Utility.Element[] tmp = _predict(node.l, l_els);
        int y_id = 0;
        for (Utility.Element el : tmp)
            Y[y_id++] = el;
        tmp = _predict(node.r, r_els);
        for (Utility.Element el : tmp)
            Y[y_id++] = el;
        return Y;
    }

    @Override
    public int[] predict(float[][][] X) {
        float[][] sX = Utility.straightenX(X);
        int[] Y = new int[sX.length];

        Utility.Element[] elements = new Utility.Element[sX.length];
        for (int i = 0; i < elements.length; ++i)
            elements[i] = new Utility.Element(sX[i], 0, i);

        Utility.Element[] el_Y = _predict(root, elements);
        for (Utility.Element el : el_Y)
            Y[el.pos] = el.cls;

        return Y;
    }
}
