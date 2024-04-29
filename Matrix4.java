public class Matrix4 {

    double[][] values;

    Matrix4(double[][] v)
    {
        this.values = v;
    }
    Matrix4()
    {
        this.values = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
    }

    public Vertex multiplyPoint(Vertex in)
    {
        double[] result_matrix = new double[]{
                in.x * this.values[0][0] + in.y * this.values[1][0] + in.z * this.values[2][0] + this.values[3][0],
                in.x * this.values[0][1] + in.y * this.values[1][1] + in.z * this.values[2][1] + this.values[3][1],
                in.x * this.values[0][2] + in.y * this.values[1][2] + in.z * this.values[2][2] + this.values[3][2],
                in.x * this.values[0][3] + in.y * this.values[1][3] + in.z * this.values[2][3] + this.values[3][3]
        };

        if (result_matrix[3] != 0)
        {
            result_matrix[0] /= result_matrix[3];
            result_matrix[1] /= result_matrix[3];
            result_matrix[2] /= result_matrix[3];
        }

        return new Vertex(
                result_matrix[0],
                result_matrix[1],
                result_matrix[2]
        );
    }

    public Matrix4 multiplyMatrix(Matrix4 other)
    {
        double[][] m = new double[4][4];
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 4; r++)
                m[r][c] = (this.values[r][0] * other.values[0][c]) +
                          (this.values[r][1] * other.values[1][c]) +
                          (this.values[r][2] * other.values[2][c]) +
                          (this.values[r][3] * other.values[3][c]);

        return new Matrix4(m);
    }

    public static Matrix4 makeIdentity()
    {
        Matrix4 m = new Matrix4();

        for (int i = 0; i < 4; i++)
            m.values[i][i] = 1;

        return m;
    }

    public static Matrix4 makeTranslation(double x, double y, double z)
    {
        Matrix4 m = Matrix4.makeIdentity();
        m.values[3][0] = x;
        m.values[3][1] = y;
        m.values[3][2] = z;
        return m;
    }

    public static Matrix4 makeZRotation(double theta)
    {
        return new Matrix4(new double[][]{
                {Math.cos(theta), Math.sin(theta), 0, 0},
                {-Math.sin(theta), Math.cos(theta), 0 , 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4 makeXRotation(double theta)
    {
        return new Matrix4(new double[][]{
                {1, 0, 0, 0},
                {0, Math.cos(theta), Math.sin(theta), 0},
                {0, -Math.sin(theta), Math.cos(theta), 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4 makeYRotation(double theta)
    {
        return new Matrix4(new double[][]{
                {Math.cos(theta), 0, Math.sin(theta), 0},
                {0, 1, 0, 0},
                {-Math.sin(theta), 0, Math.cos(theta), 0},
                {0, 0, 0, 1}
        });
    }
}
