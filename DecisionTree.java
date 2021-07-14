import java.util.*;

public class DecisionTree {
    private int max_depth;
    private Node root;
    private int _n_classes;

    static public class Node {
        boolean isleaf;
        int label;

        int feature_id;
        float test_val;

        Node l;
        Node r;
    }

    static public class Element {
        float[] features;
        int cls;
        int id;

        public Element (float[] features, int cls) {
            this.features = features;
            this.cls = cls;
        }
    }

    class SortByFeature implements Comparator<Element> {
        int f_index;

        public SortByFeature(int f_index) {
            this.f_index = f_index;
        }

        public int compare (Element a, Element b) {
            return Float.compare(a.features[f_index], b.features[f_index]);
        }
    }

    public DecisionTree(int max_depth) {
        this.max_depth = max_depth;
        root = new Node();
    }

    static float get_gini(int cnt[], int amnt) {
        float gini = 1;
        for (int i = 0; i < cnt.length; ++i)
            gini -= Math.pow(((float)cnt[i])/amnt, 2);

        return gini;
    }

    void _best_split(Node node, int depth, Element[] data) {
        int max = 0, label = 0;
        int cnt[] = new int[_n_classes];
        for (int i = 0;i < data.length; ++i) {
            ++cnt[data[i].cls];
            if (cnt[data[i].cls] > max)
                label = data[i].cls;
        }

        if (depth >= max_depth || data.length <= 1) {
            node.label = label;
            node.isleaf = true;
            return;
        }

        node.isleaf = true;
        float best_gini = get_gini(cnt, data.length);
        for (int i = 0; i < data[0].features.length; ++i) {

            Arrays.sort(data, new SortByFeature(i));

            int left[] = new int[_n_classes],
                right[] = cnt.clone();

            int c;
            float gini_l, gini_r, gini;
            for (int j = 1; j < data.length; ++j) {
                c = data[j].cls;
                ++left[c];
                --right[c];

                gini_l = get_gini(left, j);
                gini_r = get_gini(right, data.length - j);

                gini = (j * gini_l + (data.length - j) * gini_r) / data.length;

                if (data[j].features[i] == data[j - 1].features[i])
                    continue;

                if (gini < best_gini) {
                    best_gini = gini;
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

        List<Integer> l_ids = new ArrayList<Integer>(),
                      r_ids = new ArrayList<Integer>();

        for (int i = 0; i < data.length; ++i)
            if (data[i].features[node.feature_id] < node.test_val)
                l_ids.add(i);
            else
                r_ids.add(i);

        Element l_els[] = new Element[l_ids.size()],
                r_els[] = new Element[r_ids.size()];

        Iterator<Integer> it = l_ids.iterator();
        for (int i = 0; i < l_ids.size(); ++i)
            l_els[i] = data[it.next()];
        it = r_ids.iterator();
        for (int i = 0; i < r_ids.size(); ++i)
            r_els[i] = data[it.next()];

        node.l = new Node();
        node.r = new Node();
        _best_split(node.l, depth + 1, l_els);
        _best_split(node.r, depth + 1, r_els);
    }

    public void fit (float X[][], int Y[]) {
        Set<Integer> tmp = new HashSet<Integer>();
        for (int i = 0; i < Y.length; ++i)
            tmp.add(Y[i]);
        _n_classes = tmp.size();

        Element els[] = new Element[X.length];
        for (int i = 0; i < X.length; ++i)
            els[i] = new Element(X[i], Y[i]);

        _best_split(root, 1, els);
    }

    public int[] predict (float X[][]) {
        int Y[] = new int[X.length];
        for (int i = 0; i < X.length; ++i) {
            Node cur = root;
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
        Queue<Node> q = new LinkedList<Node>();

        q.add(root);
        while (q.size() > 0) {
            Node node = q.peek();

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
