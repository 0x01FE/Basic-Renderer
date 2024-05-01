import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class World3D {
    double[][] zBuffer;
    ArrayList<Object3D> objects;
    BufferedImage img;
    Matrix4 matrix;

    World3D()
    {
        this.objects = new ArrayList<>();
    }

    public void resetZBuffer(JPanel renderingPanel)
    {
        this.img = new BufferedImage(renderingPanel.getWidth(), renderingPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.zBuffer = new double[img.getWidth()][img.getHeight()];

        for (double[] row : this.zBuffer)
            Arrays.fill(row, Double.MAX_VALUE);
    }
}
