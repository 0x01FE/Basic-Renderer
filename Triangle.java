import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public class Triangle {

    Vertex[] points;
    Color color;
    Vertex normal;


    Triangle(Vertex p1, Vertex p2, Vertex p3, Color color)
    {
        this.points = new Vertex[]{p1, p2, p3};
        this.color = color;
    }

    Triangle(Vertex[] p, Color c)
    {
        this.points = p;
        this.color = c;
    }

    public Color getShade(double shade)
    {
        // it looks like this because sRGB sucks
        double redLinear = Math.pow(this.color.getRed(), 2.2) * shade;
        double greenLinear = Math.pow(this.color.getGreen(), 2.2) * shade;
        double blueLinear = Math.pow(this.color.getBlue(), 2.2) * shade;

        int red = (int) Math.pow(redLinear, 1 / 2.2);
        int green = (int) Math.pow(greenLinear, 1 / 2.2);
        int blue = (int) Math.pow(blueLinear, 1 / 2.2);

        return new Color(red, green, blue);
    }

    public static Triangle[] clipAgainstPlane(Vertex plane_point, Vertex plane_normal, Triangle view_triangle)
    {
        plane_normal.normalise();

        Vertex[] inside_points = new Vertex[3];
        int insidePointCount = 0;
        Vertex[] outside_points = new Vertex[3];
        int outsidePointCount = 0;

        // Check distances
        for (int i = 0; i < 3; i++)
        {
            double dist = dist(view_triangle.points[i], plane_normal, plane_point);

            if (dist >= 0)
            {
                inside_points[insidePointCount++] = view_triangle.points[i];
            }
            else
            {
                outside_points[outsidePointCount++] = view_triangle.points[i];
            }
        }

        // Triangle is completely outside of plane, don't draw it
        if (insidePointCount == 0)
        {
            return new Triangle[0];
        }

        // Triangle is completely inside of plane, do nothing
        if (insidePointCount == 3)
        {
            return new Triangle[]{view_triangle};
        }

        // 1 point is valid, we return 1 triangle
        if (insidePointCount == 1)
        {
            Triangle[] result = new Triangle[1];

            Vertex p1 = inside_points[0];
            Vertex p2 = Vertex.lineIntersectPlane(plane_point, plane_normal, inside_points[0], outside_points[0]);
            Vertex p3 = Vertex.lineIntersectPlane(plane_point, plane_normal, inside_points[0], outside_points[1]);

            result[0] = new Triangle(p1, p2, p3, Color.RED);

            return result;
        }

        // 2 points are valid, so we're left with a quadrilateral that needs to be split into 2 triangles
        Vertex[] t1 = new Vertex[3];
        Vertex[] t2 = new Vertex[3];

        t1[0] = inside_points[0];
        t1[1] = inside_points[1];
        t1[2] = Vertex.lineIntersectPlane(plane_point, plane_normal, inside_points[0], outside_points[0]);

        t2[0] = inside_points[1];
        t2[1] = t1[2];
        t2[2] = Vertex.lineIntersectPlane(plane_point, plane_normal, inside_points[1], outside_points[0]);

        return new Triangle[]{new Triangle(t1, Color.BLUE), new Triangle(t2, Color.GREEN)};

    }

    // Return the shortest signed distance from point to plane
    public static double dist(Vertex p, Vertex plane_normal, Vertex plane_point)
    {
        p.normalise();
        return ((plane_normal.x * p.x) + (plane_normal.y * p.y) + (plane_normal.z * p.z) - plane_normal.dot(plane_point));
    }
}
