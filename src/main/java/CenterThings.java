import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CenterThings {
    private final ImageLoading ImageLoading = new ImageLoading();
    public Point locatePoint(String image, int width, int height) {
        BufferedImage img = ImageLoading.reader(image);
        return new Point((width -  img.getWidth()) / 2, (height - img.getHeight()) / 2);
    }

    public Point centerFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Screen Size
        int x = ((screenSize.width - frame.getWidth()) / 2);
        int y = ((screenSize.height - frame.getHeight()) / 2);
        return new Point(x, y);
    }
}
