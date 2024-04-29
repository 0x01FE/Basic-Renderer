public class Vertex {
    double x;
    double y;
    double z;

    Vertex(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vertex(String x, String y, String z)
    {
        this.x = Double.parseDouble(x);
        this.y = Double.parseDouble(y);
        this.z = Double.parseDouble(z);
    }

    Vertex()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public void print()
    {
        System.out.println("X: " + this.x + " Y: " + this.y + " Z: " + this.z);
    }

    public Vertex cross(Vertex other)
    {
        return new Vertex(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public double dot(Vertex other)
    {
        return (this.x * other.x) + (this.y * other.y) + (this.z * other.z);
    }

    public void normalise()
    {
        double length = this.length();
        this.x = this.x / length;
        this.y = this.y / length;
        this.z = this.z / length;
    }

    public double length()
    {
        return Math.sqrt(this.dot(this));
    }

    public Vertex add(Vertex other)
    {
        return new Vertex(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z
        );
    }

    public Vertex subtract(Vertex other)
    {
        return new Vertex(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z
        );
    }

    public void divide(double c)
    {
        this.x /= c;
        this.y /= c;
        this.z /= c;
    }

    public Vertex multiply(double c)
    {
        return new Vertex(
                this.x * c,
                this.y * c,
                this.z * c
        );
    }
}
