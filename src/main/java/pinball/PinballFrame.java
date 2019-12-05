package pinball;

import javax.swing.*;
import java.awt.*;

public class PinballFrame extends JFrame {

    private PinballFrame()
    {
        setSize(1200, 800);
        setTitle("Pinball");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new PinballComponent(), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        new PinballFrame().setVisible(true);
    }
}
