import java.util.*;

public class FocalPointTree {
    private int max_depth;
    private float[] nodes;
    private int[] labels;
    private int[] feature_id;
    private boolean[] isLeaf;
    private int _n_classes, xcoord, ycoord;

    static public class Element {
        float[] features;
        int cls;

        public Element (float[] features, int cls) {
            this.features = features;
            this.cls = cls;
        }
    }

    class SortByFeature implements Comparator<DecisionTree.Element> {
        int f_index;

        public SortByFeature(int f_index) {
            this.f_index = f_index;
        }

        public int compare (DecisionTree.Element a, DecisionTree.Element b) {
            if (a.features[f_index] - b.features[f_index] > 0)
                return 1;
            else
                return 0;
        }
    }

    public FocalPointTree(int max_depth) {
        this.max_depth = max_depth;
        nodes = new float[1 << max_depth];
        labels = new int[nodes.length];
        feature_id = new int[nodes.length];
        isLeaf = new boolean[nodes.length];
    }

    static float get_entropy(int cnt[], int amnt) {
        float entropy = 0, tmp;
        for (int i = 0; i < cnt.length; ++i) {
            tmp = (float) cnt[i]/amnt;
            System.out.println(cnt[i]);
            if (tmp > 0)
                entropy += -tmp * Math.log(tmp);
        }

        return entropy;
    }

    void _best_split(int cur, int depth, DecisionTree.Element[] data) {
        int max = 0, label = 0;
        int cnt[] = new int[_n_classes];
        for (int i = 0;i < data.length; ++i) {
            ++cnt[data[i].cls];
            if (cnt[data[i].cls] > max)
                label = data[i].cls;
        }

        if (depth == max_depth || data.length <= 1) {
            labels[cur] = label;
            isLeaf[cur] = true;
            return;
        }

        feature_id[cur] = -1;
        float best_score = get_entropy(cnt, data.length);
        for (int i = 0; i < data[0].features.length; ++i) {

            Arrays.sort(data, new FocalPointTree.SortByFeature(i));

            int left[] = new int[_n_classes], right[] = cnt.clone();

            int c;
            float score_l, score_r, score;
            for (int j = 1; j < data.length; ++j) {
                c = data[j].cls;
                ++left[c];
                --right[c];

                score_l = get_entropy(left, j);
                score_r = get_entropy(right, data.length - j);

                score = (j * score_l + (data.length - j) * score_r) / data.length;

                if (data[j].features[i] == data[j - 1].features[i])
                    continue;

                if (score < best_score) {

                    best_score = score;
                    feature_id[cur] = i;
                    nodes[cur] = (data[j].features[i] + data[j - 1].features[i]) / 2;
                }
            }
        }

        if (feature_id[cur] == -1) {
            labels[cur] = label;
            isLeaf[cur] = true;
            return;
        }

        List<Integer> l_ids = new ArrayList<Integer>(),
                r_ids = new ArrayList<Integer>();

        for (int i = 0; i < data.length; ++i)
            if (data[i].features[feature_id[cur]] < nodes[cur])
                l_ids.add(i);
            else
                r_ids.add(i);

        DecisionTree.Element l_els[] = new DecisionTree.Element[l_ids.size()],
                r_els[] = new DecisionTree.Element[r_ids.size()];

        Iterator<Integer> it = l_ids.iterator();
        for (int i = 0; i < l_ids.size(); ++i)
            l_els[i] = data[it.next()];
        it = r_ids.iterator();
        for (int i = 0; i < r_ids.size(); ++i)
            r_els[i] = data[it.next()];

        _best_split(cur*2, depth + 1, l_els);
        _best_split(cur*2 + 1, depth + 1, r_els);
    }

    public void fit (float X[][], int Y[]) {
        Set<Integer> tmp = new HashSet<Integer>();
        for (int i = 0; i < Y.length; ++i)
            tmp.add(Y[i]);
        _n_classes = tmp.size();

        DecisionTree.Element els[] = new DecisionTree.Element[X.length];
        for (int i = 0; i < X.length; ++i)
            els[i] = new DecisionTree.Element(X[i], Y[i]);

        _best_split(1, 1, els);
    }

    public int[] predict (float X[][]) {
        int Y[] = new int[X.length];
        for (int i = 0; i < X.length; ++i) {
            int cur = 1;
            while(!isLeaf[cur]) {
                if (X[i][feature_id[cur]] < nodes[cur])
                    cur = cur*2;
                else
                    cur = cur*2 + 1;
            }
            Y[i] = labels[cur];
        }

        return Y;
    }

    public void printTree () {
        Queue<Integer> q = new LinkedList<Integer>();

        q.add(1);
        while (q.size() > 0) {
            Integer node = q.peek();

            q.remove();
            if (isLeaf[node])
                System.out.print(String.format("|       %d         |", labels[node]));
            else {
                System.out.print(String.format("| < %f feature: %d |", nodes[node], feature_id[node]));
                q.add(node * 2);
                q.add(node * 2 + 1);
            }
            System.out.println("");
        }

    }
}
