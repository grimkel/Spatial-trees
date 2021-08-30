import java.util.LinkedList;
import java.util.Queue;

public class AdaptTest {

    static void getComponent(boolean[] window, int pos, int x_size, int y_size, int id, int[] id_map) {
        Boolean[] map = new Boolean[window.length];

        Utility.Direction[] dir = new Utility.Direction[4];
        dir[0] = new Utility.Top(x_size, y_size);
        dir[1] = new Utility.Bottom(x_size, y_size);
        dir[2] = new Utility.Left(x_size, y_size);
        dir[3] = new Utility.Right(x_size, y_size);

        Queue<Integer> q = new LinkedList<Integer>();
        q.add(pos);

        while (!q.isEmpty()) {
            Integer cur = q.peek();
            map[cur] = true;
            q.remove();

            Integer tmp;
            for (Utility.Direction d : dir) {
                tmp = d.get(cur);
                if (tmp != -1 && window[cur] == window[tmp] && id_map[tmp] == 0) {
                    q.add(tmp);
                    id_map[tmp] = id;
                }
            }
        }

        Utility.print_vec(x_size, y_size, map);
    }

    static void getNeigh(boolean[] window, int pos, int x_size, int y_size) {
        int id = 1;
        int id_map[] = new int[x_size * y_size];

        for (int y = 0; y < y_size; ++y) {
            if (id_map[y * x_size] == 0)
                getComponent(window, y * x_size, x_size, y_size, id++, id_map);
            if (id_map[y * x_size + x_size - 1] == 0)
                getComponent(window, y * x_size + x_size - 1, x_size, y_size, id++, id_map);
        }
        for (int x = 0; x < x_size; ++x) {
            if (id_map[x] == 0)
                getComponent(window, x, x_size, y_size, id++, id_map);
            if (id_map[x + (y_size - 1) * x_size] == 0)
                getComponent(window, x + (y_size - 1) * x_size, x_size, y_size, id++, id_map);
        }

    }
}
