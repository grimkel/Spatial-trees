import java.util.*;

public class DecisionTree extends Tree{

    public DecisionTree(int max_depth, int min_node_size) {
        super(max_depth, min_node_size);
    }

    public void fit(float[][][] X, int[][] Y) {

        data = prepare_data(X, Y);

        _best_split(root, 1, data);
    }

    void _best_split(Utility.Node node, int depth, Utility.Element[] data) {

        int cnt[] = prepare_node(node, depth, data);
        if (node.isleaf)
            return;

        node.isleaf = true;
        float best_gini = Utility.get_gini(cnt, data.length);
        for (int i = 0; i < data[0].features.length; ++i) {

            Arrays.sort(data, new Utility.SortByFeature(i));

            int left[] = new int[_n_classes],
                    right[] = cnt.clone();

            int c;
            float gini_l, gini_r, gini;
            for (int j = 1; j < data.length; ++j) {
                c = data[j - 1].cls;
                ++left[c];
                --right[c];

                gini_l = Utility.get_gini(left, j);
                gini_r = Utility.get_gini(right, data.length - j);

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

        if (node.isleaf)
            return;

        List<Integer> l_ids = new ArrayList<Integer>(),
                r_ids = new ArrayList<Integer>();

        for (int i = 0; i < data.length; ++i)
            if (data[i].features[node.feature_id] < node.test_val)
                l_ids.add(i);
            else
                r_ids.add(i);

        Utility.Element[]
                l_els = new Utility.Element[l_ids.size()],
                r_els = new Utility.Element[r_ids.size()];

        Iterator<Integer> it = l_ids.iterator();
        for (int i = 0; i < l_ids.size(); ++i)
            l_els[i] = data[it.next()];
        it = r_ids.iterator();
        for (int i = 0; i < r_ids.size(); ++i)
            r_els[i] = data[it.next()];

        node.l = new Utility.Node();
        node.r = new Utility.Node();
        _best_split(node.l, depth + 1, l_els);
        _best_split(node.r, depth + 1, r_els);
    }
}
