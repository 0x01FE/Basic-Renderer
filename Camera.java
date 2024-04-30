public class Camera {

    Vertex position;
    Vertex direction;
    Matrix4 matrix;
    boolean go_forward, go_backwards, go_right, go_left, go_up, go_down, mouse_lock;
    double move_speed;

    Camera()
    {
        // Start at origin
        this.position = new Vertex();

        // We start looking forwards
        this.direction = new Vertex(0, 0, 1);

        this.go_forward = false;
        this.go_backwards = false;
        this.go_right = false;
        this.go_left = false;
        this.go_up = false;
        this.go_down = false;

        this.mouse_lock = true;

        this.move_speed = 0.1;
    }

    public void make_matrix(Vertex up)
    {
        this.matrix = quick_inverse(this.point_at(up));
    }

    public Matrix4 point_at(Vertex up)
    {
        Vertex target = this.position.add(this.direction);

        // Calculate new forward
        Vertex new_forward = target.subtract(this.position);
        new_forward.normalise();

        // Calculate new up
        Vertex temp_v = new_forward.multiply(up.dot(new_forward));
        Vertex new_up = up.subtract(temp_v);
        new_up.normalise();

        // Calculate new right
        Vertex new_right = new_up.cross(new_forward);

        // Construct matrix for translations
        return new Matrix4(new double[][]{
                {new_right.x, new_right.y, new_right.z, 0},
                {new_up.x, new_up.y, new_up.z, 0},
                {new_forward.x, new_forward.y, new_forward.z, 0},
                {this.position.x, this.position.y, this.position.z, 1}
        });
    }

    // quick because it only works for this type of matrix
    private static Matrix4 quick_inverse(Matrix4 m)
    {
        double[][] v = m.values;
        return new Matrix4(new double[][]{
                {v[0][0], v[1][0], v[2][0], 0},
                {v[0][1], v[1][1], v[2][1], 0},
                {v[0][2], v[1][2], v[2][2], 0},
                {
                    -(v[3][0] * v[0][0] + v[3][1] * v[0][1] + v[3][2] * v[0][2]),
                    -(v[3][0] * v[1][0] + v[3][1] * v[1][1] + v[3][2] * v[1][2]),
                    -(v[3][0] * v[2][0] + v[3][1] * v[2][1] + v[3][2] * v[2][2]),
                    1
                }
        });
    }

    public void move()
    {
        if (this.go_forward)
            this.position = this.position.add(this.direction.multiply(this.move_speed));
        if (this.go_backwards)
            this.position = this.position.subtract(this.direction.multiply(this.move_speed));

        if (this.go_right)
            this.position.x += this.move_speed;
        if (this.go_left)
            this.position.x -= this.move_speed;

        if (this.go_up)
            this.position.y -= this.move_speed;
        if (this.go_down)
            this.position.y += this.move_speed;
    }

}
