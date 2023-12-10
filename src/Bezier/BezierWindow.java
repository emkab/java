package Bezier;

import res.Vector2;
import res.Window;

import java.awt.*;
import java.awt.event.KeyEvent;
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
        palette.put("Box", new Color(113, 113, 122));
        palette.put("Fill", new Color(39, 39, 42));
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

    boolean drawLines = false;
    boolean drawBoundingBox = false;

    public void update() {
        write("Drag points to edit the curve", new Vector2((float) -width / 2 + 5, (float) -height / 2 + 15), palette.get("Line"), 16f, onscreen);
        write("Right-click to toggle connective lines", new Vector2((float) -width / 2 + 5, (float) -height / 2 + 30), palette.get("Line"), 12f, onscreen);
        write("Press 'b' to toggle bounding box", new Vector2((float) -width / 2 + 5, (float) -height / 2 + 45), palette.get("Line"), 12f, onscreen);

        graphBezier(palette.get("Curve"), onscreen);

        if (drawLines) {
            drawLine(controlPoints.get("P0").pos, controlPoints.get("P1").pos, palette.get("Line"), 4, onscreen);
            drawLine(controlPoints.get("P2").pos, controlPoints.get("P3").pos, palette.get("Line"), 4, onscreen);
        }

        if (drawBoundingBox) boundingBox(Color.red, Color.green, onscreen);

        for (String pointKey : controlPoints.keySet()) {
            Vector2 pos = controlPoints.get(pointKey).pos.get();
            drawCircle(pos, controlPointRadius, palette.get(pointKey), palette.get("Fill"), 10, onscreen);
            pos.y += (float) (controlPointRadius * 1.5);
            write(pointKey, pos, palette.get(pointKey), 12f, onscreen);
        }
    }

    private void graphBezier(Color color, Graphics2D g2d) {
        g2d.setColor(color);
        Vector2 P0 = controlPoints.get("P0").pos;
        Vector2 P1 = controlPoints.get("P1").pos;
        Vector2 P2 = controlPoints.get("P2").pos;
        Vector2 P3 = controlPoints.get("P3").pos;
        for (double t = 0.0; t <= 1.0; t += 0.001) {
            Vector2 pos = screen.normalToScreen(getPointFromT(t));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect((int) pos.x, (int) pos.y, 1, 1);
        }
    }

    private void boundingBox(Color xColor, Color yColor, Graphics2D g2d) {
        HashMap<String, double[]> roots = getSecondDerivativeRoots();
        double[] rootsX = roots.get("X");
        double[] rootsY = roots.get("Y");

        Vector2 maxT = getPointFromT(1);
        Vector2 minT = getPointFromT(0);
        HashMap<String, Vector2> points = new HashMap<>();
        points.put("maxT", maxT);
        points.put("minT", minT);
        if (rootsX[0] != -1) points.put("xRoot1", getPointFromT(rootsX[0]));
        if (rootsX[1] != -1) points.put("xRoot2", getPointFromT(rootsX[1]));
        if (rootsY[0] != -1) points.put("yRoot1", getPointFromT(rootsY[0]));
        if (rootsY[1] != -1) points.put("yRoot2", getPointFromT(rootsY[1]));

        double limit = 1000000.0;

        Vector2 point0 = points.get("xRoot1");
        Vector2 point1 = points.get("xRoot2");
        Vector2 point2 = points.get("yRoot1");
        Vector2 point3 = points.get("yRoot2");

        double maxX = Math.max(Math.max(maxT.x, minT.x), Math.max(Math.max(point0 != null ? point0.x : -limit, point1 != null ? point1.x : -limit), Math.max(point2 != null ? point2.x : -limit, point3 != null ? point3.x : -limit)));
        double maxY = Math.max(Math.max(maxT.y, minT.y), Math.max(Math.max(point0 != null ? point0.y : -limit, point1 != null ? point1.y : -limit), Math.max(point2 != null ? point2.y : -limit, point3 != null ? point3.y : -limit)));
        double minX = Math.min(Math.min(maxT.x, minT.x), Math.min(Math.min(point0 != null ? point0.x : limit, point1 != null ? point1.x : limit), Math.min(point2 != null ? point2.x : limit, point3 != null ? point3.x : limit)));
        double minY = Math.min(Math.min(maxT.y, minT.y), Math.min(Math.min(point0 != null ? point0.y : limit, point1 != null ? point1.y : limit), Math.min(point2 != null ? point2.y : limit, point3 != null ? point3.y : limit)));

        Vector2 max = new Vector2((float) maxX, (float) maxY);
        Vector2 min = new Vector2((float) minX, (float) minY);


        max = screen.normalToScreen(max);
        min = screen.normalToScreen(min);

        drawRect(min, max, palette.get("Box"), 4, onscreen);

        for (double root : rootsX) {
            if (root != -1) {
                Vector2 pos = getPointFromT(root);
                drawCircle(pos, controlPointRadius / 2, xColor, palette.get("Fill"), 10, onscreen);
            }
        }
        for (double root : rootsY) {
            if (root != -1) {
                Vector2 pos = getPointFromT(root);
                drawCircle(pos, controlPointRadius / 2, yColor, palette.get("Fill"), 10, onscreen);
            }
        }
    }

    private void drawRect(Vector2 pos1, Vector2 pos2, Color color, float strokeSize, Graphics2D g2d) {
        double width = pos1.distanceX(pos2);
        double height = pos1.distanceY(pos2);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRect((int) pos1.x, (int) pos1.y, (int) width, (int) height);
    }

    private Vector2 getPointFromT(double t) {
        Vector2 P0 = controlPoints.get("P0").pos;
        Vector2 P1 = controlPoints.get("P1").pos;
        Vector2 P2 = controlPoints.get("P2").pos;
        Vector2 P3 = controlPoints.get("P3").pos;

        double x = Math.pow(1 - t, 3) * P0.x + 3 * Math.pow((1 - t), 2) * t * P1.x + 3 * (1 - t) * Math.pow(t, 2) * P2.x + Math.pow(t, 3) * P3.x;
        double y = Math.pow(1 - t, 3) * P0.y + 3 * Math.pow((1 - t), 2) * t * P1.y + 3 * (1 - t) * Math.pow(t, 2) * P2.y + Math.pow(t, 3) * P3.y;
        return new Vector2((float) x, (float) y);
    }

    private Vector2 getFirstDerivativeFromT(double t) {
        Vector2 P0 = controlPoints.get("P0").pos;
        Vector2 P1 = controlPoints.get("P1").pos;
        Vector2 P2 = controlPoints.get("P2").pos;
        Vector2 P3 = controlPoints.get("P3").pos;

        double x = 3 * Math.pow((1 - t), 2) * (P1.x - P0.x) + 6 * (1 - t) * t * (P2.x - P1.x) + 3 * Math.pow(t, 2) * (P3.x - P2.x);
        double y = 3 * Math.pow((1 - t), 2) * (P1.y - P0.y) + 6 * (1 - t) * t * (P2.y - P1.y) + 3 * Math.pow(t, 2) * (P3.y - P2.y);
        return new Vector2((float) x, (float) y);
    }

    private Vector2 getSecondDerivativeFromT(double t) {
        Vector2 P0 = controlPoints.get("P0").pos;
        Vector2 P1 = controlPoints.get("P1").pos;
        Vector2 P2 = controlPoints.get("P2").pos;
        Vector2 P3 = controlPoints.get("P3").pos;

        double x = 6 * (1 - t) * (P2.x - 2 * P1.x + P0.x) + 6 * t * (P3.x - 2 * P2.x + P1.x);
        double y = 6 * (1 - t) * (P2.y - 2 * P1.y + P0.y) + 6 * t * (P3.y - 2 * P2.y + P1.y);
        return new Vector2((float) x, (float) y);
    }

    private void printSecondDerivativeRoots() {
        HashMap<String, double[]> roots = getSecondDerivativeRoots();
        double[] rootsX = roots.get("X");
        double[] rootsY = roots.get("Y");
        System.out.println("X: " + rootsX[0] + ", " + rootsX[1] + ", Y: " + rootsY[0] + ", " + rootsY[1]);
    }

    private HashMap<String, double[]> getSecondDerivativeRoots() {
        Vector2 P0 = controlPoints.get("P0").pos;
        Vector2 P1 = controlPoints.get("P1").pos;
        Vector2 P2 = controlPoints.get("P2").pos;
        Vector2 P3 = controlPoints.get("P3").pos;

        float ax = -3 * P0.x + 9 * P1.x - 9 * P2.x + 3 * P3.x;
        float ay = -3 * P0.y + 9 * P1.y - 9 * P2.y + 3 * P3.y;

        float bx = 6 * P0.x - 12 * P1.x + 6 * P2.x;
        float by = 6 * P0.y - 12 * P1.y + 6 * P2.y;

        float cx = -3 * P0.x + 3 * P1.x;
        float cy = -3 * P0.y + 3 * P1.y;

        double[] rootsX = new double[2];
        rootsX[0] = checkRoot((-bx + Math.sqrt(Math.pow(bx, 2) - 4 * ax * cx)) / (2 * ax));
        rootsX[1] = checkRoot((-bx - Math.sqrt(Math.pow(bx, 2) - 4 * ax * cx)) / (2 * ax));

        double[] rootsY = new double[2];
        rootsY[0] = checkRoot((-by + Math.sqrt(Math.pow(by, 2) - 4 * ay * cy)) / (2 * ay));
        rootsY[1] = checkRoot((-by - Math.sqrt(Math.pow(by, 2) - 4 * ay * cy)) / (2 * ay));

        HashMap<String, double[]> roots = new HashMap<>();
        roots.put("X", rootsX);
        roots.put("Y", rootsY);

        return roots;
    }

    private double checkRoot(double root) {
        if (root <= 1.0 && root >= 0.0) return root;
        return -1;
    }

    public void drawCircle(int x, int y, int r, Color color, Color fillColor, float strokeSize, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(x, y);
        g2d.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        super.drawCircle((int) newPos.x - r / 2, (int) newPos.y - r / 2, r, color, fillColor, g2d);
    }

    public void drawCircle(Vector2 pos, int r, Color color, Color fillColor, float strokeSize, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(pos.x, pos.y);
        g2d.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        super.drawCircle((int) newPos.x - r / 2, (int) newPos.y - r / 2, r, color, fillColor, g2d);
    }

    public void write(String string, Vector2 pos, Color color, float fontSize, Graphics2D g2d) {
        Vector2 newPos = screen.normalToScreen(pos.x, pos.y);
        g2d.setColor(color);
        g2d.setFont(g2d.getFont().deriveFont(fontSize));
        g2d.drawString(string, newPos.x, newPos.y);
    }

    public void drawLine(Vector2 pos1, Vector2 pos2, Color color, float strokeSize, Graphics2D g2d) {
        pos1 = screen.normalToScreen(pos1);
        pos2 = screen.normalToScreen(pos2);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine((int) pos1.x, (int) pos1.y, (int) pos2.x, (int) pos2.y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'b') drawBoundingBox = !drawBoundingBox;
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
