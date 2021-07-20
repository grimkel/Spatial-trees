import java.util.*;

public class SpatialGainTree {
    private int max_depth;
    private DecisionTree.Node root;
    private DecisionTree.Node[] id2leaf;
    private DecisionTree.Element[] allData;
    private int _n_classes, x_size, y_size;
    private float alpha;
    private Direction dir[];

    class SortByFeature implements Comparator<DecisionTree.Element> {
        int f_index;

        public SortByFeature(int f_index) {
            this.f_index = f_index;
        }

        public int compare (DecisionTree.Element a, DecisionTree.Element b) {
            return Float.compare(a.features[f_index], b.features[f_index]);
        }
    }

    public SpatialGainTree(int max_depth, float alpha, int x_size, int y_size) {
        this.max_depth = max_depth;
        root = new DecisionTree.Node();

        this.x_size = x_size;
        this.y_size = y_size;
        this.alpha = alpha;

        this.dir = new Direction[4];
        dir[0] = new Top();
        dir[1] = new Bottom();
        dir[2] = new Left();
        dir[3] = new Right();
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

    public interface Direction {
        int get (int id);
    }

    public class Top implements Direction {

        public int get (int id) {
            if (id / y_size == x_size - 1)
                return -1;
            return id + y_size;
        }
    }

    public class Bottom implements Direction {

        public int get (int id) {
            if (id < y_size)
                return -1;
            return id - y_size;
        }
    }

    public class Left implements Direction {

        public int get (int id) {
            if (id % y_size == 0)
                return -1;
            return id - 1;
        }
    }

    public class Right implements Direction {

        public int get (int id) {
            if (id % y_size == y_size - 1)
                return -1;
            return id + 1;
        }
    }

    void _best_split(DecisionTree.Node node, int depth, DecisionTree.Element[] data, int neighbor_score) {
        int max = 0, label = 0;
        int cnt[] = new int[_n_classes];
        for (int i = 0;i < data.length; ++i) {
            ++cnt[data[i].cls];
            if (cnt[data[i].cls] > max) {
                max = cnt[data[i].cls];
                label = data[i].cls;
            }
        }

        if (depth >= max_depth || data.length <= 1) {
            node.label = label;
            node.isleaf = true;
            return;
        }

        node.isleaf = true;

        float best_sig = alpha * neighbor_score/ data.length;
        float base = get_entropy(cnt, data.length);

        DecisionTree.Node tmp_node = new DecisionTree.Node();

        int best_split_indx = 0, best_left = 0, best_right = neighbor_score;
        for (int i = 0; i < data[0].features.length; ++i) {

            Arrays.sort(data, new SpatialGainTree.SortByFeature(i));

            DecisionTree.Node[] tmp_id2leaf = id2leaf.clone();
            int left[] = new int[_n_classes], right[] = cnt.clone();
            int left_neighbor_score = 0, right_neighbor_score = neighbor_score;
            for (int j = 1; j < data.length; ++j) {
                int c = data[j - 1].cls;

                ++left[c];
                --right[c];

                tmp_id2leaf[data[j-1].id] = tmp_node;

                float entropy_l = get_entropy(left, j);
                float entropy_r = get_entropy(right, data.length - j);

                float entropy_decrease = base - (j * entropy_l + (data.length - j) * entropy_r) / data.length;

                for (int t = 0; t < 4; ++t) {
                    int id = dir[t].get(data[j - 1].id);
                    if (id == -1)
                        continue;
                    if (tmp_id2leaf[id] == node && allData[id].cls == c)
                        right_neighbor_score -= 2;
                    else if (tmp_id2leaf[id] == tmp_node && allData[id].cls == c)
                        left_neighbor_score += 2;
                }

                float nsar = (float) (left_neighbor_score + right_neighbor_score) / data.length;

                float sig = (1.f - alpha) * entropy_decrease + alpha * nsar;

                if (data[j].features[i] == data[j - 1].features[i])
                    continue;

                if (sig > best_sig) {
                    best_split_indx = j;
                    best_sig = sig;
                    best_left = left_neighbor_score;
                    best_right = right_neighbor_score;
                    node.isleaf = false;
                    node.feature_id = i;
                    node.test_val = (data[j].features[i] + data[j - 1].features[i]) / 2;
                }
            }
        }

        if (node.isleaf) {
            node.label = label;
            node.isleaf = true;
            return;
        }

        node.l = new DecisionTree.Node();
        node.r = new DecisionTree.Node();

        DecisionTree.Element
                l_els[] = new DecisionTree.Element[best_split_indx],
                r_els[] = new DecisionTree.Element[data.length - best_split_indx];

        int l_indx = 0, r_indx = 0;
        for (int i = 0; i < data.length; ++i) {
            if (data[i].features[node.feature_id] < node.test_val) {
                id2leaf[data[i].id] = node.l;
                l_els[l_indx++] = data[i];
            }
            else {
                id2leaf[data[i].id] = node.r;
                r_els[r_indx++] = data[i];
            }
        }

        _best_split(node.l, depth + 1, l_els, best_left);
        _best_split(node.r, depth + 1, r_els, best_right);
    }

    public void fit (float X[][], int Y[]) {
        id2leaf = new DecisionTree.Node[X.length];
        Arrays.setAll(id2leaf, (index) -> root);

        Set<Integer> tmp = new HashSet<Integer>();
        for (int i = 0; i < Y.length; ++i)
            tmp.add(Y[i]);
        _n_classes = tmp.size();

        DecisionTree.Element els[] = new DecisionTree.Element[X.length];
        for (int i = 0; i < X.length; ++i) {
            els[i] = new DecisionTree.Element(X[i], Y[i]);
            els[i].id = i;
        }

        allData = els;

        int score = 0;
        for (int i = 0; i < els.length; ++i)
            for (int j = 0; j < 4; ++j) {
                int id = dir[j].get(els[i].id);
                if (id == -1)
                    continue;
                if (els[id].cls == els[i].cls)
                    ++score;
            }

        _best_split(root, 1, els, score);
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
