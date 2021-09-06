import java.util.Arrays;
import java.util.Random;

public class GenData {

    float[][][] X;
    int[][] Y;

    int size;

    int MAX_BOUND = 100;

    float first_class_bounds_min, first_class_bounds_max;
    float second_class_bounds_min, second_class_bounds_max;

    GenData (int size) {
        X = new float[size][size][1];
        Y = new int[size][size];

        this.size = size;

        Random rd = new Random();

        first_class_bounds_min = rd.nextFloat() * 100;
        first_class_bounds_max = first_class_bounds_min + rd.nextFloat() * (100 - first_class_bounds_min);

        second_class_bounds_min = rd.nextFloat() * 100;
        second_class_bounds_max = second_class_bounds_min + rd.nextFloat() * (100 - second_class_bounds_min);

        for (int i = 0; i < size; ++i)
            for (int j = 0; j < size; ++j)
                X[i][j][0] = first_class_bounds_min + rd.nextFloat() * (first_class_bounds_max - first_class_bounds_min);
    }

    GenData(int size,
            float first_class_bounds_min,
            float first_class_bounds_max,
            float second_class_bounds_min,
            float second_class_bounds_max) {

        X = new float[size][size][1];
        Y = new int[size][size];

        this.size = size;

        Random rd = new Random();

        this.first_class_bounds_min = first_class_bounds_min;
        this.first_class_bounds_max = first_class_bounds_max;
        this.second_class_bounds_min = second_class_bounds_min;
        this.second_class_bounds_max = second_class_bounds_max;

        for (int i = 0; i < size; ++i)
            for (int j = 0; j < size; ++j)
                X[i][j][0] = first_class_bounds_min + rd.nextFloat() * (first_class_bounds_max - first_class_bounds_min);
    }

    void addRectanglePatch(int x_size,int y_size) {
        Random rd = new Random();
        int x = Math.abs(rd.nextInt()) % size, y = Math.abs(rd.nextInt()) % size;
        for (int i = x; i < Math.min(x_size + x, size); ++i)
            for (int j = y; j < Math.min(y_size + y, size); ++j) {
                X[i][j][0] = second_class_bounds_min + rd.nextFloat() * (second_class_bounds_max - second_class_bounds_min);
                Y[i][j] = 1;
            }
    }

    void addCirclePatch(int r) {
        Random rd = new Random();
        int x = Math.abs(rd.nextInt()) % size, y = Math.abs(rd.nextInt()) % size;
        for (int i = 0; i < size; ++i)
            for (int j = 0; j < size; ++j) {
                if (Math.sqrt(Math.pow(i - x, 2) + Math.pow(j - y, 2)) <= r) {
                    X[i][j][0] = second_class_bounds_min + rd.nextFloat() * (second_class_bounds_max - second_class_bounds_min);
                    Y[i][j] = 1;
                }
            }
    }

    void addNoise(float chance) {
        Random rd = new Random();
        for (int i = 0; i < size; ++i)
            for (int j = 0; j < size; ++j)
                if (Math.random() < chance)
                    if (Y[i][j] == 1)
                        X[i][j][0] = first_class_bounds_min + rd.nextFloat() * (first_class_bounds_max - first_class_bounds_min);
                    else
                        X[i][j][0] = second_class_bounds_min + rd.nextFloat() * (second_class_bounds_max - second_class_bounds_min);

    }

    void printData() {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j)
                System.out.printf("%f ",X[i][j][0]);
            System.out.print('\n');
        }
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j)
                System.out.printf("%d",Y[i][j]);
            System.out.print('\n');
        }
    }
}
