import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Object3D
{
    Triangle[] triangles;

    Object3D(Triangle[] t)
    {
        this.triangles = t;
    }

    Object3D()
    {
        this.triangles = null;
    }

    public void draw(Graphics2D g2, Matrix4 world_matrix, Matrix4 map_projection, Camera camera, Vertex light_direction)
    {
        BufferedImage img = new BufferedImage(Renderer.WIDTH, Renderer.HEIGHT, BufferedImage.TYPE_INT_ARGB);

        double[][] zBuffer = new double[img.getHeight()][img.getWidth()];
        for (double[] row : zBuffer)
            Arrays.fill(row, Double.MAX_VALUE);

        for (Triangle t : this.triangles)
        {
            Path2D path = new Path2D.Double();
            g2.setColor(t.color);

            Vertex[] transformed_points = new Vertex[3];
            Vertex[] view_points = new Vertex[3];
            Vertex[] projected_points = new Vertex[3];

            for (int i = 0; i < 3; i ++) {
                transformed_points[i] = world_matrix.multiplyPoint(t.points[i]);
                transformed_points[i].z += 4;
            }

            // Calculate Normals and stuff
            Vertex normal = getNormal(transformed_points);

            // Normalise normal vector?
            normal.normalise();

            // Back Face Culling
            Vertex camera_ray = transformed_points[0].subtract(camera.position);

            if (normal.dot(camera_ray) > 0 && Renderer.BACK_FACE_CULLING)
                continue;

            // Convert from World Space --> View Space (camera)
            for (int i = 0; i < 3; i++)
                view_points[i] = camera.matrix.multiplyPoint(transformed_points[i]);

            // Clipping against the near plane
            Vertex plane_point = new Vertex(0, 0, 0.1);
            Vertex plane_normal = new Vertex(0, 0, 1);
            Triangle view_triangle = new Triangle(view_points, t.color);
            Triangle[] clipped_triangles = Triangle.clipAgainstPlane(plane_point, plane_normal, view_triangle);

            // Project Points from 3D --> 2D
            for (Triangle clippedTriangle : clipped_triangles) {
                for (int i = 0; i < t.points.length; i++) {
                    // Project
                    Vertex projected_point = map_projection.multiplyPoint(clippedTriangle.points[i]);

                    // X/Y are inverted
                    projected_point.x *= -1;
                    projected_point.y *= -1;

                    // Scale Point
                    projected_point.x += 1;
                    projected_point.y += 1;

                    projected_point.x *= 0.5 * Renderer.WIDTH;
                    projected_point.y *= 0.5 * Renderer.HEIGHT;


                    // Store for projected points for rasterisation
                    projected_points[i] = projected_point;
                }

                // Clipping against screen edges
                Triangle projected_triangle = new Triangle(projected_points, t.color);
                projected_triangle.print();

                Queue<Triangle> trianglesToDraw = new LinkedList<>();
                trianglesToDraw.add(projected_triangle);
                int newTrianglesCount = 1;

                for (int plane = 0; plane < 4; plane++)
                {
                    while (newTrianglesCount > 0)
                    {
                        Triangle[] newTriangles = new Triangle[0];

                        Triangle test = trianglesToDraw.remove();
                        newTrianglesCount--;

                        switch (plane)
                        {
                            case 0 -> newTriangles = Triangle.clipAgainstPlane(new Vertex(0, 0, 0), new Vertex(0, 1, 0), test);
                            case 1 -> newTriangles = Triangle.clipAgainstPlane(new Vertex(0, Renderer.HEIGHT - 1, 0), new Vertex(0, -1, 0), test);
                            case 2 -> newTriangles = Triangle.clipAgainstPlane(new Vertex(0, 0, 0), new Vertex(1, 0, 0), test);
                            case 3 -> newTriangles = Triangle.clipAgainstPlane(new Vertex(Renderer.WIDTH - 1, 0, 0), new Vertex(-1, 0, 0), test);
                        }

                        for (Triangle newTriangle : newTriangles)
                            trianglesToDraw.add(newTriangle);
                    }
                    newTrianglesCount = trianglesToDraw.size();
                }

                for (Triangle triToDraw : trianglesToDraw)
                {
                    // Draw Triangle Lines
                    if (Renderer.DRAW_WIRES) {
                        g2.setColor(triToDraw.color);
                        for (int i = 0; i < 3; i++) {
                            Vertex projected_point = triToDraw.points[i];

                            if (i == 0) {
                                path.moveTo(projected_point.x, projected_point.y);
                            } else {
                                path.lineTo(projected_point.x, projected_point.y);
                            }
                        }

                        path.closePath();
                        g2.draw(path);
                    }

                    // Rasterisation
                    if (Renderer.DRAW_FACES) {

                        Vertex v1 = triToDraw.points[0];
                        Vertex v2 = triToDraw.points[1];
                        Vertex v3 = triToDraw.points[2];


                        // Calculate Ranges
                        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                        int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                        int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                        double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                        for (int y = minY; y <= maxY; y++) {
                            for (int x = minX; x <= maxX; x++) {

                                double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                                double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                                double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {

                                    double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;

                                    if (zBuffer[y][x] > depth) {
                                        // Now that we know we're coloring this pixel we do light calculations
                                        light_direction.normalise();
                                        double shade = light_direction.dot(normal);

                                        img.setRGB(x, y, t.getShade(shade).getRGB());
                                        zBuffer[y][x] = depth;
                                    }
                                }
                            }
                        }
                    }
                }
                }



            if (Renderer.DRAW_FACES)
                g2.drawImage(img, 0, 0, null);
            }

    }

    private static Vertex getNormal(Vertex[] points) {
        Vertex line1 = points[1].subtract(points[0]);
        Vertex line2 = points[2].subtract(points[0]);

        return line1.cross(line2);
    }

    public void loadFromOBJ(String file_path)
    {
        Scanner reader;
        File OBJ;
        ArrayList<Vertex> v = new ArrayList<>();
        ArrayList<Vertex> vn = new ArrayList<>();
        ArrayList<Triangle> t = new ArrayList<>();
        String line;
        String[] f_data_temp;
        int[] f_data = new int[3]; // Face Data
        int i;


        // Try to open file
        try {
            OBJ = new File(file_path);
            reader = new Scanner(OBJ);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: OBJ File \"" + file_path + "\" not found.");
            e.printStackTrace();
            return;
        }

        // Read data
        while (reader.hasNextLine())
        {
            line = reader.nextLine();

            String[] data = line.split(" ");

            switch (data[0])
            {
                case "v":
                    v.add(new Vertex(data[1], data[2], data[3]));
                    break;
                case "vn":
                    vn.add(new Vertex(data[1], data[2], data[3]));
                    break;
                case "f":
                    // Sample face data : "f 5//1 3//1 1//1" where each thing is v//normal_v

                    for (i = 1; i < 4; i++)
                    {
                        f_data_temp = data[i].split("//");
                        f_data[i-1] = Integer.parseInt(f_data_temp[0]) - 1;
                    }

                    t.add(new Triangle(v.get(f_data[0]), v.get(f_data[1]), v.get(f_data[2]), Color.WHITE));
                    break;
            }

        }

        this.triangles = new Triangle[t.size()];
        this.triangles = t.toArray(this.triangles);
    }

    public void randomRGB()
    {
        for (Triangle t : triangles)
        {
            int r = ThreadLocalRandom.current().nextInt(0, 256);
            int g = ThreadLocalRandom.current().nextInt(0, 256);
            int b = ThreadLocalRandom.current().nextInt(0, 256);
            t.color = new Color(r, g, b);
        }
    }

    public void print()
    {
        for (Triangle t : this.triangles)
        {
            for (Vertex p : t.points)
            {
                p.print();
            }
            System.out.println();
        }
    }
}
