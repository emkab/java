package Bezier;

import res.BaseEntity;
import res.Entity;
import res.Vector2;
import res.Window;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class BezierWindow extends Window {
    public BezierWindow(int _width, int _height, double _graphicsScale, int _fps) {
        super(_width, _height, _graphicsScale, _fps);
    }

    public void init(String title) {
        setup();
        super.init(title);
    }

    private HashMap<String, ControlPoint> controlPoints;
    private int controlPointRadius = 20;

    private void setup() {
        controlPoints = new HashMap<>();
        controlPoints.put("P0", new ControlPoint());
        controlPoints.put("P1", new ControlPoint());
        controlPoints.put("P2", new ControlPoint());
        controlPoints.put("P3", new ControlPoint());
    }

    public void update() {
        for (ControlPoint point : controlPoints.values()) {
            drawCircle(point.pos, controlPointRadius, Color.blue, onscreen);
        }
    }

    public void drawCircle(int x, int y, int r, Color color, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(x, y);
        super.drawCircle((int) newPos.x, (int) newPos.y, r, color, g2d);
    }

    public void drawCircle(Vector2 pos, int r, Color color, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(pos.x, pos.y);
        super.drawCircle((int) newPos.x, (int) newPos.y, r, color, g2d);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        for (ControlPoint point : controlPoints.values()) {
            if (point.pos.distance(screen.screenToNormal(e.getX() - controlPointRadius, e.getY() - (float) controlPointRadius / 2)) <= controlPointRadius) {
                point.pos = screen.screenToNormal(e.getX() - controlPointRadius, e.getY() - (float) controlPointRadius / 2);
                break;
            }
        }
    }
}
