package Bezier;

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
    private HashMap<String, Color> controlPointColors;
    private int controlPointRadius = 20;

    private void setup() {
        controlPoints = new HashMap<>();
        controlPoints.put("P0", new ControlPoint());
        controlPoints.put("P1", new ControlPoint());
        controlPoints.put("P2", new ControlPoint());
        controlPoints.put("P3", new ControlPoint());
        controlPointColors = new HashMap<>();
        controlPointColors.put("P0", Color.red);
        controlPointColors.put("P1", Color.blue);
        controlPointColors.put("P2", Color.green);
        controlPointColors.put("P3", Color.yellow);
    }

    public void update() {
        for (String pointKey : controlPoints.keySet()) {
            Vector2 pos = controlPoints.get(pointKey).pos.get();
            drawCircle(pos, controlPointRadius, controlPointColors.get(pointKey), onscreen);
            Vector2 textPos;
            textPos = pos;
            textPos.y += controlPointRadius * 2;
            write(pointKey, textPos, controlPointColors.get(pointKey), onscreen);
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

    public void write(String string, Vector2 pos, Color color, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(pos.x, pos.y);
        onscreen.setColor(color);
        onscreen.drawString(string, newPos.x, newPos.y);
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
