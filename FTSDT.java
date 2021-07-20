import java.util.*;

public class FTSDT {
    private int max_depth;
    private DecisionTree.Node root;
    private int _n_classes, x_size, y_size, max_s;

    class SortByFeature implements Comparator<DecisionTree.Element> {
        int f_index;

        public SortByFeature(int f_index) {
            this.f_index = f_index;
        }

        public int compare (DecisionTree.Element a, DecisionTree.Element b) {
            return Float.compare(a.features[f_index], b.features[f_index]);
        }
    }

    public FTSDT(int max_depth, int max_s, int x_size, int y_size) {
        this.max_depth = max_depth;
        root = new DecisionTree.Node();

        this.x_size = x_size;
        this.y_size = y_size;
        this.max_s = max_s;
    }

    static float get_entropy(int cnt[], int amnt) {
        float entropy = 0, tmp;
        for (int i = 0; i < cnt.length; ++i) {
            tmp = (float) cnt[i]/amnt;
            if (tmp > 0)
                entropy += -tmp * Math.log(tmp);
        }

        return entropy;
    }

    float focal_func(int ind[], DecisionTree.Element[] data, int s, int cur) {
        int pos_x = cur % x_size, pos_y = cur/x_size;
        int lshift = Math.max(0, pos_x - s),
            rshift = Math.min(x_size - 1, pos_x + s);
        int top = Math.max(0, pos_y - s),
            bot = Math.min(y_size - 1, pos_y + s);

        int foc = 0, y, x, w_sum = 0;
        for (int i = 0; i < ind.length; ++i) {
            y = data[i].id / x_size;
            x = data[i].id % x_size;
            if ((top <= y && y <= bot) && (lshift <= x && x <= rshift)) {
                foc += ind[i];
                ++w_sum;
            }
        }

        return (float) foc / w_sum;
    }

    int[] node_split(DecisionTree.Element[] data, float d, int f_index, int s) {
        int ind[] = new int[data.length];
        for (int i = 0; i < data.length; ++i)
            ind[i] = Float.compare(data[i].features[f_index], d);

        int split[] = new int[data.length];

        for (int i = 0; i < data.length; ++i) {
            float foc = focal_func(ind, data, s, data[i].id);
            boolean foc_test = (ind[i] <= 0) ^ (Float.compare(0.f, foc) < 0);

            if (foc_test)
                split[i] = 0;
            else
                split[i] = 1;
        }

        return split;
    }

    void _best_split(DecisionTree.Node node, int depth, DecisionTree.Element[] data) {
        int max = 0, label = 0;
        int cnt[] = new int[_n_classes];
        Set<Integer> tmp = new HashSet<Integer>();
        for (int i = 0;i < data.length; ++i) {
            ++cnt[data[i].cls];
            tmp.add(data[i].cls);
            if (cnt[data[i].cls] > max)
                label = data[i].cls;
        }
        node.isleaf = true;
        if (depth >= max_depth || data.length <= 1 || tmp.size() == 1) {
            node.label = label;
            return;
        }

        int best_split[] = new int[data.length], best_l = 0, best_r = 0;
        float base = get_entropy(cnt, data.length), best_ig = 0;

        for (int i = 0; i < data[0].features.length; ++i) {
            Arrays.sort(data, new FTSDT.SortByFeature(i));
            for (int s = 1; s <= max_s; ++s) {
                int cnt0[] = new int[_n_classes],
                    cnt1[] = new int[_n_classes];

                for (int j = 1; j < data.length; ++j) {

                    float d = (data[j - 1].features[i] + data[j].features[i])/2.f;
                    int split[] = node_split(data, d, i, s);

                    int l = 0,
                        r = 0;

                    for (int t = 0; t < split.length; ++t) {
                        if (split[t] == 0) {
                            ++cnt0[data[t].cls];
                            ++l;
                        }
                        else {
                            ++cnt1[data[t].cls];
                            ++r;
                        }
                    }

                    float entropy_l = get_entropy(cnt0, l);
                    float entropy_r = get_entropy(cnt1, r);

                    float ig = base - (j * entropy_l + (data.length - j) * entropy_r) / data.length;

                    if (ig > best_ig) {
                        best_ig = ig;
                        best_split = split;
                        best_l = l;
                        best_r = r;
                        node.isleaf = false;
                        node.feature_id = i;
                        node.test_val = d;
                    }
                }
            }
        }

        if (node.isleaf) {
            node.label = label;
            return;
        }

        node.l = new DecisionTree.Node();
        node.r = new DecisionTree.Node();

        DecisionTree.Element
                l_els[] = new DecisionTree.Element[best_l],
                r_els[] = new DecisionTree.Element[best_r];

        int l_indx = 0, r_indx = 0;
        for (int i = 0; i < data.length; ++i) {
            if (best_split[i] == 0)
                l_els[l_indx++] = data[i];
            else
                r_els[r_indx++] = data[i];
        }

        _best_split(node.l, depth + 1, l_els);
        _best_split(node.r, depth + 1, r_els);
    }

    public void fit (float X[][], int Y[]) {
        Set<Integer> tmp = new HashSet<Integer>();
        for (int i = 0; i < Y.length; ++i)
            tmp.add(Y[i]);
        _n_classes = tmp.size();

        DecisionTree.Element els[] = new DecisionTree.Element[X.length];
        for (int i = 0; i < X.length; ++i) {
            els[i] = new DecisionTree.Element(X[i], Y[i]);
            els[i].id = i;
        }

        _best_split(root, 1, els);
    }

    public int[] predict (float X[][]) {
        int Y[] = new int[X.length];
        for (int i = 0; i < X.length; ++i) {
            DecisionTree.Node cur = root;
            while(!cur.isleaf) {
                if (X[i][cur.feature_id] < cur.test_val)
                    cur = cur.l;
                else
                    cur = cur.r;
            }
            Y[i] = cur.label;
        }

        return Y;
    }

    public void printTree () {
        Queue<DecisionTree.Node> q = new LinkedList<DecisionTree.Node>();

        q.add(root);
        while (q.size() > 0) {
            DecisionTree.Node node = q.peek();

            q.remove();
            if (node.isleaf)
                System.out.print(String.format("|       %d         |", node.label));
            else {
                System.out.print(String.format("| < %f feature: %d |", node.test_val, node.feature_id));
                q.add(node.l);
                q.add(node.r);
            }
            System.out.println("");
        }

    }
}
