import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class Renderer {

    static final int HEIGHT = 1080;
    static final int WIDTH = 1920;
    static final int FOV = 90;
    static final double LOOK_SPEED = 15;
    static final boolean SHOW_AXIS_LINES = false;
    static final Color BACKGROUND_COLOR = Color.BLACK;
    static final boolean ROTATE_MODE = false;
    static final int TARGET_FPS = 60;
    // Overrides Target FPS
    static final boolean FPS_TEST = false;
    static final boolean DRAW_FACES = true;
    static final boolean DRAW_WIRES = true;

    // Should ALMOST ALWAYS BE TRUE
    static final boolean BACK_FACE_CULLING = true;

    static final boolean RENDER_GIF = false;
    static final String RENDER_FILE = "render.gif";
    // Time between frames in GIF, 100 units to 1 second
    // e.x. DELAY = 5; means a frame every 1000/100*5 = 50 ms
    static final int DELAY = 5;
    //Size (squared) that the GIF should be cropped to
    //  Center of square is at center of screen
    static final int GIF_SIZE = 400;
    //the number of frames to capture
    static final int FRAMES_TO_CAPTURE = 500;

    public static void main(String[] args)
    {
        final double ASPECT_RATIO = (double) HEIGHT / WIDTH;
        final double FOV_RAD = 1.0 / (Math.tan(Math.toRadians(FOV / 2.0)));
        final double Z_NEAR = 0.1;
        final double Z_FAR = 1000;
        final double Q = Z_FAR/(Z_FAR - Z_NEAR);

        Point world_rotation = new Point(0, 0);
        Point camera_rotation = new Point(0, 0);
        ArrayList<Object3D> world_objects = new ArrayList<>();
        Camera camera = new Camera();
        camera.position = new Vertex(0, 0, 0);
        Vertex light_direction = new Vertex(0, 0, -1);

        // X, Y, Z Axis Lines
        if (SHOW_AXIS_LINES)
        {
            Line3D z_line = new Line3D(new Vertex[]{
                    new Vertex(0, 0, -3),
                    new Vertex(0, 0, 3)
            },
                    Color.BLUE);

            world_objects.add(z_line);

            Line3D x_line = new Line3D(new Vertex[]{
                    new Vertex(-3, 0, 0),
                    new Vertex(3, 0, 0)
            },
                    Color.RED);

            world_objects.add(x_line);

            Line3D y_line = new Line3D(new Vertex[]{
                    new Vertex(0, -3, 0),
                    new Vertex(0, 3, 0)
            },
                    Color.GREEN);

            world_objects.add(y_line);
        }


        // Load objects for the world
        Object3D c = new Object3D();
        c.loadFromOBJ("objs/sphere.obj");

        world_objects.add(c);


        // Do the Computer Graphics
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();

        JPanel renderingPanel = new JPanel()
        {
            public void paintComponent(Graphics g)
            {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Calculate POV transform
                Matrix4 map_projection = new Matrix4(new double[][]{
                        {ASPECT_RATIO * FOV_RAD, 0, 0, 0},
                        {0, FOV_RAD, 0, 0},
                        {0, 0, Q, 1},
                        {0, 0, -Z_NEAR * Q, 0}
                });

                // Calculate rotation Transforms
                double theta = Math.toRadians(world_rotation.y);
                Matrix4 z_rotation = Matrix4.makeZRotation(theta);

                theta = Math.toRadians(world_rotation.x);
                Matrix4 x_rotation = Matrix4.makeXRotation(theta);

                Matrix4 world_matrix;
                world_matrix = z_rotation.multiplyMatrix(x_rotation);
                world_matrix.multiplyMatrix(Matrix4.makeTranslation(0, 0, 4));

                Vertex up = new Vertex(0, 1, 0);

                double camera_rotation_y_radians = Math.toRadians(camera_rotation.y);
                camera.direction = new Vertex(0, 0, 1);
                Matrix4 y_camera_rotation = Matrix4.makeYRotation(camera_rotation_y_radians);
                camera.direction = y_camera_rotation.multiplyPoint(camera.direction);

                camera.make_matrix(up);

                // Render Stored 3D Objects
                for (Object3D object : world_objects)
                {
                    object.draw(g2, world_matrix, map_projection, camera, light_direction, this);
                }
            }
        };

        // add Mouse Listener for rotating
        renderingPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {}

            @Override
            public void mouseMoved(MouseEvent e)
            {
                double window_width = frame.getWidth();
                double window_height = frame.getHeight();

                double half_width = window_width / 2.0;
                double half_height = window_height / 2.0;

                int mouse_x = e.getX() - (renderingPanel.getWidth() / 2);
                int mouse_y = e.getY() - (renderingPanel.getHeight() / 2);

                if (camera.mouse_lock)
                {
                    camera_rotation.y += (mouse_x / half_width) * LOOK_SPEED;

                    // Code to move mouse to 0,0
                    try {
                        Robot tim = new Robot();

                        tim.mouseMove((int)half_width, (int)half_height);
                    } catch (AWTException exception)
                    {
                        exception.printStackTrace();
                        System.exit(1);
                    }

                }
            }
        });

        renderingPanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e)
            {
                // TODO : Make shift increase camera move speed (maybe just for Z axis?)
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> camera.go_forward = true;
                    case KeyEvent.VK_S -> camera.go_backwards = true;
                    case KeyEvent.VK_D -> camera.go_right = true;
                    case KeyEvent.VK_A -> camera.go_left = true;
                    case KeyEvent.VK_SPACE -> camera.go_up = true;
                    case KeyEvent.VK_CONTROL -> camera.go_down = true;
                    case KeyEvent.VK_ESCAPE -> {
                            camera.mouse_lock = !camera.mouse_lock;
                            if (camera.mouse_lock)
                            {
                                frame.setCursor(frame.getToolkit().createCustomCursor(
                                        new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new java.awt.Point(0, 0),
                                        "null"));
                            }
                            else
                            {
                                frame.setCursor(Cursor.getDefaultCursor());
                            }
                    }
                }
            }


            @Override
            public void keyReleased(KeyEvent e)
            {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> camera.go_forward = false;
                    case KeyEvent.VK_S -> camera.go_backwards = false;
                    case KeyEvent.VK_D -> camera.go_right = false;
                    case KeyEvent.VK_A -> camera.go_left = false;
                    case KeyEvent.VK_SPACE -> camera.go_up = false;
                    case KeyEvent.VK_CONTROL -> camera.go_down = false;
                }
            }
        });
        renderingPanel.setFocusable(true);
        renderingPanel.requestFocusInWindow();

        pane.add(renderingPanel, BorderLayout.CENTER);

        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setCursor(frame.getToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new java.awt.Point(0, 0),
                "null"));

        // Rotate mode needs 60~ fps


        long sleep_time = (long)Math.floor(1000.0 / TARGET_FPS);
        if (FPS_TEST)
            sleep_time = 0;

        int frames = 0;

        long currentTime;

        long frameStartTime = System.currentTimeMillis();
        long frameEndTime;
        double frameDeltaTime;
        double FPS = 0;

        long moveStartTime = System.currentTimeMillis();
        long moveEndTime;
        double moveDeltaTime;

        Giffer giffer;
        try {
            giffer = new Giffer(RENDER_FILE, DELAY, true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        int frames_captured = 0;

        while (true)
        {
            // Draw Frame
            renderingPanel.repaint();
            frames++;

            currentTime = System.currentTimeMillis();

            frameEndTime = currentTime;
            moveEndTime = currentTime;

            // If rotate mode, rotate
            if (ROTATE_MODE)
            {
                world_rotation.x = (currentTime / 100.0) * 4;
                world_rotation.y = (currentTime / 100.00) * 4;
            }

            // Capture frame to render GIF
            if (RENDER_GIF)
            {
                BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = bi.createGraphics();
                renderingPanel.print(g);
                g.dispose();

                int center_x = WIDTH/2;
                int center_y = HEIGHT/2;
                int left_x = center_x - GIF_SIZE/2;
                int left_y = center_y - GIF_SIZE/2;
                BufferedImage cropped = bi.getSubimage(left_x, left_y, GIF_SIZE, GIF_SIZE);
                //captured_frames[frames_captured] = cropped;
                try {
                    giffer.addImage(cropped);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                frames_captured++;

                if (frames_captured == FRAMES_TO_CAPTURE) {
                    System.out.println("---------------------------------Started drawing gif!");
                    try {
                        //Giffer.generateFromBI(captured_frames, "render.gif", 2, true);
                        giffer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("---------------------------------Finished drawing gif!");
                    System.exit(1);
                }
            }

            // Calculate FPS
            frameDeltaTime = (double) (frameEndTime - frameStartTime);

            if (frameDeltaTime >= 1000)
            {
                FPS = frames / (frameDeltaTime/1000);
                System.out.println("FPS: " + FPS);
                if (RENDER_GIF)
                    System.out.println("Captured frames: " + frames_captured);

                frames = 0;
                frameStartTime = System.currentTimeMillis();
            }

            // Movement shouldn't be tied to FPS
            moveDeltaTime = (double) (moveEndTime - moveStartTime);

            if (moveDeltaTime >= 25)
            {
                camera.move();
                moveStartTime = System.currentTimeMillis();
            }

            // Calculate sleep time to ensure program is meeting target FPS
            if (FPS != 0 && !FPS_TEST) {
                if (FPS < TARGET_FPS * 0.8 && FPS > TARGET_FPS * 0.4) {
                    sleep_time = (long) ((sleep_time * TARGET_FPS) / FPS);
                } else if (FPS < TARGET_FPS * 0.4) {
                    sleep_time = 0;
                }
            }

            try {
                Thread.sleep(sleep_time);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}




