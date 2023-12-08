package Bezier;

import res.Vector2;
import res.Window;

import java.awt.*;

public class BezierWindow extends Window {
    public BezierWindow(int _width, int _height, double _graphicsScale, int _fps) {
        super(_width, _height, _graphicsScale, _fps);
    }

    public void update() {
        drawCircle(0, 0, 20, Color.white, onscreen);
    }

    public void drawCircle(int x, int y, int r, Color color, Graphics2D g2d) {
        Vector2 pos = screen.normalToScreen(x, y);
        super.drawCircle((int) pos.x, (int) pos.y, r, color, g2d);
    }
}
