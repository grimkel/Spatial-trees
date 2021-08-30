import java.util.HashSet;
import java.util.Set;

public abstract class Tree{
    protected int _n_classes, min_node_size, max_depth;

    protected Utility.Node root;
    protected Utility.Element[] data;

    Tree (int max_depth, int min_node_size) {
        this.max_depth = max_depth;
        this.min_node_size = min_node_size;
        root = new Utility.Node();
    }

    int[] prepare_node (Utility.Node node, int depth, Utility.Element[] data) {
        int start_cls = data[0].cls;
        boolean all_same = true;

        int max = 0, label = 0;
        int[] cnt = new int[_n_classes];
        for (Utility.Element el : data) {
            ++cnt[el.cls];
            if (cnt[el.cls] > max) {
                max = cnt[el.cls];
                label = el.cls;
            }
            if (start_cls != el.cls)
                all_same = false;
        }

        node.label = label;
        if (data.length < min_node_size || all_same || depth >= max_depth)
            node.isleaf = true;

        return cnt;
    }

    Utility.Element[] prepare_data (float[][][] X, int[][] Y) {
        float[][] sX = Utility.straightenX(X);
        int[] sY = Utility.straightenY(Y);

        Set<Integer> tmp = new HashSet<Integer>();
        for (int y : sY) tmp.add(y);
        _n_classes = tmp.size();

        Utility.Element[] els = new Utility.Element[sX.length];
        for (int i = 0; i < sX.length; ++i)
            els[i] = new Utility.Element(sX[i], sY[i], i);

        return els;
    }

    abstract public void fit(float[][][] X, int[][] Y);

    public int[] predict(float[][][] X) {
        float[][] sX = Utility.straightenX(X);
        int[] Y = new int[sX.length];
        for (int i = 0; i < sX.length; ++i) {
            Utility.Node cur = root;
            while(!cur.isleaf) {
                if (Float.compare(sX[i][cur.feature_id], cur.test_val) <= 0)
                    cur = cur.l;
                else
                    cur = cur.r;
            }
            Y[i] = cur.label;
        }

        return Y;
    };
}
