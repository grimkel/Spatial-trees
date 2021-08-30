import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        float X[][];
        int Y[] = new int[150];

        float X_tmp[][][] = new float[][][]{
                {{30, 10},{30, 10},{30, 30},{30, 30},{30, 30},{30, 30},{30, 10},{30, 10}},
                {{30, 10},{10, 10},{30, 30},{30, 30},{30, 30},{10, 30},{30, 10},{30, 10}},
                {{30, 10},{30, 10},{30, 30},{30, 30},{30, 30},{30, 30},{30, 10},{30, 10}},
                {{30, 10},{10, 10},{30, 30},{30, 30},{30, 30},{10, 30},{30, 10},{30, 10}},
                {{10, 20},{10, 20},{10, 20},{10, 20},{10, 20},{10, 20},{10, 20},{10, 20}},
                {{10, 20},{30, 20},{10, 20},{10, 20},{10, 20},{30, 20},{10, 20},{30, 20}},
                {{10, 20},{10, 20},{10, 20},{10, 20},{10, 20},{10, 20},{10, 20},{10, 20}},
                {{10, 20},{10, 20},{30, 20},{10, 20},{10, 20},{30, 20},{10, 20},{10, 20}}};

        int Y_tmp[][] = new int[][]{
                {1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,1,1},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0}};

        X = new float[32][2];
        for (int i = 0 ; i < 4; ++i)
            for (int j = 0 ; j < 4; ++j) {
                X[i * 4 + j] = X_tmp[i][j];
                Y[i * 4 + j] = Y_tmp[i][j];

                int id = i + 4, jd = j + 4;
                X[16 + i * 4 + j] = X_tmp[id][jd];
                Y[16 + i * 4 + j] = Y_tmp[id][jd];
            }

        SpatialGainTree tree = new SpatialGainTree(3, 0.3f,4, 8);

        tree.fit(X, Y);

        X = new float[32][2];
        for (int i = 4 ; i < 8; ++i)
            for (int j = 0 ; j < 4; ++j) {
                X[(i - 4) * 4 + j] = X_tmp[i][j];

                int jd = j + 4;
                X[16 + (i - 4) * 4 + j] = X_tmp[i - 4][jd];
            }

        Y = tree.predict(X);

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j)
                System.out.print(String.format("%d ", Y[i * 4 + j]));
            System.out.print("\n");
        }

        System.out.println("");

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j)
                System.out.print(String.format("%d ", Y[16 + i * 4 + j]));
            System.out.print("\n");
        }

        tree.printTree();

        //--------------------------------------

        //for (int i = 0; i < 150; ++i)
        //    System.out.println(String.format("%d %d", res[i], Y[i]));
    }
}
