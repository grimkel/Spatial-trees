import java.util.Arrays;

public class SpatialGainTree extends Tree{
    int x_size, y_size;
    float alpha;
    Utility.Direction[] dir;
    Utility.Node[] id2leaf;

    public SpatialGainTree(int max_depth, int min_node_size, float alpha) {
        super(max_depth, min_node_size);

        this.alpha = alpha;
    }

    public void fit(float[][][] X, int[][] Y) {
        this.x_size = X[0].length;
        this.y_size = X.length;

        data = prepare_data(X, Y);

        this.dir = new Utility.Direction[4];
        dir[0] = new Utility.Top(x_size, y_size);
        dir[1] = new Utility.Bottom(x_size, y_size);
        dir[2] = new Utility.Left(x_size, y_size);
        dir[3] = new Utility.Right(x_size, y_size);

        id2leaf = new Utility.Node[data.length];
        Arrays.setAll(id2leaf, (index) -> root);

        int score = 0;
        for (Utility.Element el : data)
            for (int j = 0; j < 4; ++j) {
                int id = dir[j].get(el.pos);
                if (id == -1)
                    continue;
                if (data[id].cls == el.cls)
                    ++score;
            }

        _best_split(root, 1, data.clone(), score);
    }

    void _best_split(Utility.Node node, int depth, Utility.Element[] data, int neighbor_score) {

        int[] cnt = prepare_node(node, depth, data);
        if (node.isleaf)
            return;

        node.isleaf = true;
        float best_sig = alpha * neighbor_score / data.length;
        float base = Utility.get_entropy(cnt, data.length);

        Utility.Node tmp_node = new Utility.Node();

        int best_split_indx = 0, best_left = 0, best_right = neighbor_score;
        for (int i = 0; i < data[0].features.length; ++i) {

            Arrays.sort(data, new Utility.SortByFeature(i));

            Utility.Node[] tmp_id2leaf = id2leaf.clone();

            int[] left = new int[_n_classes], right = cnt.clone();
            int left_neighbor_score = 0, right_neighbor_score = neighbor_score;
            for (int j = 1; j < data.length; ++j) {
                int c = data[j - 1].cls;

                ++left[c];
                --right[c];

                tmp_id2leaf[data[j-1].pos] = tmp_node;

                float entropy_l = Utility.get_entropy(left, j);
                float entropy_r = Utility.get_entropy(right, data.length - j);

                float entropy_decrease = base - (j * entropy_l + (data.length - j) * entropy_r) / data.length;

                for (int t = 0; t < 4; ++t) {
                    int id = dir[t].get(data[j - 1].pos);
                    if (id == -1)
                        continue;

                    if (tmp_id2leaf[id] == node && this.data[id].cls == c)
                        right_neighbor_score -= 2;
                    else if (tmp_id2leaf[id] == tmp_node && this.data[id].cls == c)
                        left_neighbor_score += 2;
                }

                float nsar = (float) (left_neighbor_score + right_neighbor_score) / data.length;

                float sig = (1.f - alpha) * entropy_decrease + alpha * nsar;

                if (Float.compare(data[j].features[i], data[j - 1].features[i]) == 0)
                    continue;

                if (sig > best_sig) {
                    best_split_indx = j;
                    best_sig = sig;
                    best_left = left_neighbor_score;
                    best_right = right_neighbor_score;
                    node.isleaf = false;
                    node.feature_id = i;
                    node.test_val = data[j].features[i];
                }
            }
        }

        if (node.isleaf)
            return;

        node.l = new Utility.Node();
        node.r = new Utility.Node();

        Utility.Element[]
                l_els = new Utility.Element[best_split_indx],
                r_els = new Utility.Element[data.length - best_split_indx];

        int l_indx = 0, r_indx = 0;
        for (Utility.Element el : data) {
            if (Float.compare(el.features[node.feature_id], node.test_val) < 0) {
                id2leaf[el.pos] = node.l;
                l_els[l_indx++] = el;
            } else {
                id2leaf[el.pos] = node.r;
                r_els[r_indx++] = el;
            }
        }

        _best_split(node.l, depth + 1, l_els, best_left);
        _best_split(node.r, depth + 1, r_els, best_right);
    }
}
