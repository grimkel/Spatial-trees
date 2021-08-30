import java.util.Comparator;

public class Utility {

    interface Direction {
        int get (int id);
    }

    static abstract class AbsDir implements Direction {
        int x_size, y_size;
        AbsDir (int x_size, int y_size) {
            this.x_size = x_size;
            this.y_size = y_size;
        }
    }

    static class Top extends AbsDir {

        Top(int x_size, int y_size) {
            super(x_size, y_size);
        }

        public int get (int id) {
            if (id / y_size == x_size - 1)
                return -1;
            return id + y_size;
        }
    }

    static class Bottom extends AbsDir {

        Bottom(int x_size, int y_size) {
            super(x_size, y_size);
        }

        public int get (int id) {
            if (id < y_size)
                return -1;
            return id - y_size;
        }
    }

    static class Left extends AbsDir {

        Left(int x_size, int y_size) {
            super(x_size, y_size);
        }

        public int get (int id) {
            if (id % y_size == 0)
                return -1;
            return id - 1;
        }
    }

    static class Right extends AbsDir {

        Right(int x_size, int y_size) {
            super(x_size, y_size);
        }

        public int get (int id) {
            if (id % y_size == y_size - 1)
                return -1;
            return id + 1;
        }
    }

    static public class Node {
        boolean isleaf;
        int label;

        int feature_id;
        float test_val;

        Node l;
        Node r;
    }

    public static class Element {
        float[] features;
        int cls;
        int pos;

        public Element (float[] features, int cls, int pos) {
            this.features = features;
            this.cls = cls;
            this.pos = pos;
        }
    }

    public static class SortByFeature implements Comparator<Element> {
        int f_index;

        public SortByFeature(int f_index) {
            this.f_index = f_index;
        }

        public int compare (Element a, Element b) {
            return Float.compare(a.features[f_index], b.features[f_index]);
        }
    }

    public static float get_gini(int[] cnt, int amnt) {
        float gini = 1;
        for (int cls_cnt : cnt)
            gini -= Math.pow(((float) cls_cnt) / amnt, 2);

        return gini;
    }

    public static int[] straightenY(int[][] Y) {
        int[] res = new int[Y.length * Y[0].length];
        for (int i = 0; i < Y.length; ++i)
            System.arraycopy(Y[i], 0, res, i * Y.length, Y[0].length);
        return res;
    }

    public static float[][] straightenX(float[][][] X) {
        float[][] res = new float[X.length * X[0].length][X[0][0].length];
        for (int i = 0; i < X.length; ++i)
            System.arraycopy(X[i], 0, res, i * X.length, X[0].length);

        return res;
    }

    static float get_entropy(int[] cnt, int amnt) {
        float entropy = 0, tmp;
        for (int cls_cnt : cnt) {
            tmp = (float) cls_cnt / amnt;
            if (tmp > 0)
                entropy += -tmp * Math.log(tmp);
        }

        return entropy;
    }

    static float distance (float[] f, float[] s) {
        float res = 0.f;
        for (int i = 0; i < f.length; ++i)
            res += Math.pow(f[i] - s[i], 2);
        return (float) Math.sqrt(res);
    }

    static <T> void print_vec(int x_size, int y_size, T[] arr) {
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < y_size; ++i) {
            for (int j = 0; j < x_size; ++j) {
                System.out.print(arr[i * x_size + j]);
                System.out.print("\t\t");
            }
            System.out.print('\n');
        }
        System.out.println("--------------------------------------------------");
    }
}
