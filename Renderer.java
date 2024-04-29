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
    static final boolean SHOW_AXIS_LINES = false;
    static final Color BACKGROUND_COLOR = Color.BLACK;
    static final boolean ROTATE_MODE = true;
    static final int TARGET_FPS = 60;
    // Overrides Target FPS
    static final boolean FPS_TEST = false;
    static final boolean DRAW_FACES = false;
    static final boolean DRAW_WIRES = true;


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


        final double DRAG_SPEED = 50;
        final double ASPECT_RATIO = (double) HEIGHT / WIDTH;
        final double FOV_RAD = 1.0 / (Math.tan(Math.toRadians(FOV / 2.0)));
        final double Z_NEAR = 0.1;
        final double Z_FAR = 1000;
        final double Q = Z_FAR/(Z_FAR - Z_NEAR);

        Point mouse = new Point(0, 0);
        ArrayList<Object3D> world_objects = new ArrayList<Object3D>();
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


        Object3D c = new Object3D();
        c.loadFromOBJ("objs/sphere.obj");
//        c.randomRGB();

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
                // TODO : Refactor these transforms into an object maybe?
                double pitch = Math.toRadians(mouse.y);
                Matrix4 z_r_transform = new Matrix4(new double[][]{
                        {Math.cos(pitch), Math.sin(pitch), 0, 0},
                        {-Math.sin(pitch), Math.cos(pitch), 0 , 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}
                });

                double roll = Math.toRadians(mouse.x);
                Matrix4 x_r_transform = new Matrix4(new double[][]{
                        {1, 0, 0, 0},
                        {0, Math.cos(roll), Math.sin(roll), 0},
                        {0, -Math.sin(roll), Math.cos(roll), 0},
                        {0, 0, 0, 1}
                });

                Matrix4 world_matrix;
                world_matrix = z_r_transform.multiplyMatrix(x_r_transform);
//                world_matrix.multiplyMatrix(Matrix4.makeTranslation(0, 0, 0));

                camera.direction = new Vertex(0, 0, 1);
                Vertex up = new Vertex(0, 1, 0);
                camera.make_matrix(up);

                // Render Stored 3D Objects
                for (Object3D object : world_objects)
                {
                    object.draw(g2, world_matrix, map_projection, camera, light_direction);
                }
            }
        };

        // add Mouse Listener for rotating
        renderingPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!ROTATE_MODE) {
                    double yi = DRAG_SPEED / renderingPanel.getHeight();
                    double xi = DRAG_SPEED / renderingPanel.getWidth();

                    mouse.x = e.getX() * xi;
                    mouse.y = e.getY() * yi;

                    renderingPanel.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        renderingPanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e)
            {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> camera.go_forward = true;
                    case KeyEvent.VK_S -> camera.go_backwards = true;
                    case KeyEvent.VK_D -> camera.go_right = true;
                    case KeyEvent.VK_A -> camera.go_left = true;
                    case KeyEvent.VK_SPACE -> camera.go_up = true;
                    case KeyEvent.VK_CONTROL -> camera.go_down = true;
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

        // Rotate mode needs 60~ fps


        long sleep_time = (long)Math.floor(1000.0 / TARGET_FPS);
        if (FPS_TEST)
            sleep_time = 0;

        int frames = 0;

        long currentTime;

        long frameStartTime = System.currentTimeMillis();
        long frameEndTime = 0;
        double frameDeltaTime = 0;
        double FPS = 0;

        long moveStartTime = System.currentTimeMillis();
        long moveEndTime = 0;
        double moveDeltaTime = 0;

        Giffer giffer;
        try {
            giffer = new Giffer(RENDER_FILE, DELAY, true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        long frameRenderStart;
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
                mouse.x = (currentTime / 100.0) * 4;
                mouse.y = (currentTime / 100.00) * 4;
            }

            // Capture frame to render GIF
            if (RENDER_GIF)
            {
                frameRenderStart = System.currentTimeMillis();
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

                try {
                    currentTime = System.currentTimeMillis();
                    Thread.sleep(Math.max(0, sleep_time - (currentTime - frameRenderStart)));
                } catch (Exception e) {
                    e.printStackTrace();
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
                    System.out.println("Captured frames: " + Integer.toString(frames_captured));

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
            if (FPS != 0) {
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




