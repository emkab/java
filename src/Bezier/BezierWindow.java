package Bezier;

import res.Vector2;
import res.Window;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class BezierWindow extends Window {
    public BezierWindow(int _width, int _height, double _graphicsScale, int _fps) {
        super(_width, _height, _graphicsScale, _fps);
    }

    Font rubik;

    public void init(String title) {
        setup();
        super.init(title);

        try {
            rubik = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResource("rubik.ttf").openStream());
            GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            genv.registerFont(rubik);
            rubik = rubik.deriveFont(Font.PLAIN, 12f);
            onscreen.setFont(rubik);
            System.out.println(onscreen.getFont().toString());
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }

        super.startThreads();
    }

    private HashMap<String, Color> palette;
    private HashMap<String, ControlPoint> controlPoints;
    private int controlPointRadius = 20;

    private void setup() {
        palette = new HashMap<>();
        palette.put("Background", new Color(15, 23, 42));
        super.backgroundColor = palette.get("Background");
        palette.put("Curve", new Color(203, 213, 225));
        palette.put("Line", new Color(148, 163, 184));
        palette.put("P0", new Color(220, 38, 38));
        palette.put("P1", new Color(37, 99, 235));
        palette.put("P2", new Color(234, 179, 8));
        palette.put("P3", new Color(22, 163, 74));

        controlPoints = new HashMap<>();
        controlPoints.put("P0", new ControlPoint(new Vector2(100, 50)));
        controlPoints.put("P1", new ControlPoint(new Vector2(75, -50)));
        controlPoints.put("P2", new ControlPoint(new Vector2(-75, -50)));
        controlPoints.put("P3", new ControlPoint(new Vector2(-100, 50)));
    }

    boolean drawLines = true;

    public void update() {
        write("Drag points to edit the curve", new Vector2((float) -width / 2 + 5, (float) -height / 2 + 15), palette.get("Line"), 16f, onscreen);
        write("(Right-click to toggle connective lines)", new Vector2((float) -width / 2 + 5, (float) -height / 2 + 30), palette.get("Line"), 12f, onscreen);

        for (String pointKey : controlPoints.keySet()) {
            Vector2 pos = controlPoints.get(pointKey).pos.get();
            drawCircle(pos, controlPointRadius, palette.get(pointKey), onscreen);
            pos.y += (float) (controlPointRadius * 1.5);
            write(pointKey, pos, palette.get(pointKey), 12f, onscreen);
        }

        if (drawLines) {
            drawLine(controlPoints.get("P0").pos, controlPoints.get("P1").pos, palette.get("Line"), onscreen);
            drawLine(controlPoints.get("P2").pos, controlPoints.get("P3").pos, palette.get("Line"), onscreen);
        }

        graphBezier(palette.get("Curve"), onscreen);
    }

    private void graphBezier(Color color, Graphics2D g2d) {
        g2d.setColor(color);
        Vector2 P0 = controlPoints.get("P0").pos;
        Vector2 P1 = controlPoints.get("P1").pos;
        Vector2 P2 = controlPoints.get("P2").pos;
        Vector2 P3 = controlPoints.get("P3").pos;
        for (double t = 0.0; t <= 1.0; t += 0.001) {

            double x = Math.pow(1 - t, 3) * P0.x + 3 * Math.pow((1 - t), 2) * t * P1.x + 3 * (1 - t) * Math.pow(t, 2) * P2.x + Math.pow(t, 3) * P3.x;
            double y = Math.pow(1 - t, 3) * P0.y + 3 * Math.pow((1 - t), 2) * t * P1.y + 3 * (1 - t) * Math.pow(t, 2) * P2.y + Math.pow(t, 3) * P3.y;
            Vector2 pos = screen.normalToScreen((float) x, (float) y);

            g2d.drawRect((int) pos.x, (int) pos.y, 1, 1);
        }
    }

    public void drawCircle(int x, int y, int r, Color color, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(x, y);
        super.drawCircle((int) newPos.x, (int) newPos.y, r, color, g2d);
    }

    public void drawCircle(Vector2 pos, int r, Color color, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(pos.x, pos.y);
        super.drawCircle((int) newPos.x - controlPointRadius / 2, (int) newPos.y - controlPointRadius / 2, r, color, g2d);
    }

    public void write(String string, Vector2 pos, Color color, float fontSize, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(pos.x, pos.y);
        g2d.setColor(color);
        g2d.setFont(g2d.getFont().deriveFont(fontSize));
        g2d.drawString(string, newPos.x, newPos.y);
    }

    public void drawLine(Vector2 pos1, Vector2 pos2, Color color, Graphics2D g2d) {
        pos1 = screen.normalToScreen(pos1);
        pos2 = screen.normalToScreen(pos2);
        g2d.setColor(color);
        g2d.drawLine((int) pos1.x, (int) pos1.y, (int) pos2.x, (int) pos2.y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == 3) {
            drawLines = !drawLines;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        for (ControlPoint point : controlPoints.values()) {
            if (point.pos.distance(screen.screenToNormal(e.getX(), e.getY())) <= controlPointRadius) {
                point.pos = screen.screenToNormal(e.getX(), e.getY());
                break;
            }
        }
    }
}
