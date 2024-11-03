package Bezier;

import res.Screen;
import res.Vector2;
import res.Window;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
    private Float[] arcLength;

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

        arcLength = new Float[1000];
    }

    boolean drawLines = false;
    boolean drawBoundingBox = false;

    public void update() {
        bindControlPointToEdge();
        write("Drag points to edit the curve", new Vector2((float) -width / 2 + 10, (float) -height / 2 + 15), palette.get("Line"), 16f, onscreen);
        write("Right-click to toggle connective lines", new Vector2((float) -width / 2 + 10, (float) -height / 2 + 30), palette.get("Line"), 12f, onscreen);
        write("Press 'b' to toggle bounding box", new Vector2((float) -width / 2 + 10, (float) -height / 2 + 45), palette.get("Line"), 12f, onscreen);
        write("Press 'space' to toggle animation", new Vector2((float) -width / 2 + 10, (float) -height / 2 + 60), palette.get("Line"), 12f, onscreen);
        write("Press 'ctrl' and use the mouse wheel to change animation speed", new Vector2((float) -width / 2 + 10, (float) -height / 2 + 75), palette.get("Line"), 12f, onscreen);
        write("Press 'shift' and use the mouse wheel to change the amount of circles in the animation", new Vector2((float) -width / 2 + 10, (float) -height / 2 + 90), palette.get("Line"), 12f, onscreen);

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
    private void bindControlPointToEdge() {
        for (ControlPoint point : controlPoints.values()) {
            if (Math.abs(point.pos.x) > (width / 2.0)) {
                point.pos.x = (float) ((width / 2.0) * Math.signum(point.pos.x) + (controlPointRadius * -1 * Math.signum(point.pos.x)));
            }
            if (Math.abs(point.pos.y) > (height / 2.0)) {
                point.pos.y = (float) ((height / 2.0) * Math.signum(point.pos.y) + (controlPointRadius * -1 * Math.signum(point.pos.y)));
            }
        }
    }
    boolean playAnimation;
    private void graphBezier(Color color, Graphics2D g2d) {
        g2d.setColor(color);
        int i = 0;
        for (double t = 0.0; t <= 1.0; t += 0.001) {
            Vector2 pos = getPointFromT(t);
            calcArcLength(i, pos);
            pos = screen.normalToScreen(pos);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect((int) pos.x, (int) pos.y, 1, 1);
            i++;
        }
       if (playAnimation) dotAnimation();
    }

    private void calcArcLength(int i, Vector2 point) {
        if (i != 0) arcLength[i] = arcLength[i -1] + point.distance(getPointFromT(getTFromI(i - 1)));
        if (i == 0) arcLength[i] = 0f;
    }

    private double getTFromI(int i) {return i / 1000.0;}

    private float distToT(float distance) {
        float length = arcLength[arcLength.length - 1];
        int n = arcLength.length;

        if (distance >= 0 && distance <= length) {
            for (int i = 0; i < n; i++) {
                if (distance >= arcLength[i] && distance <= arcLength[i+1]) {
                    return (float) ((getTFromI(i) + getTFromI(i + 1)) / 2);
                }
            }
        }
        return distance/length;
    }
    float[] dots;
    int dotNum = 10;
    float animationSpeed = 500;
    private void dotAnimation() {
        for (int i = 0; i < dots.length; i++) {
            Vector2 pos = getPointFromT(distToT(dots[i]));
            pos = screen.normalToScreen(pos);
            drawCircle((int) pos.x, (int) pos.y, controlPointRadius / 2, palette.get("Line"), palette.get("Fill"), onscreen);
            dots[i] += arcLength[arcLength.length - 1] / animationSpeed;
            if (dots[i] > arcLength[arcLength.length - 1]) dots[i] = 0f;
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

    boolean ctrlPressed = false;
    boolean shiftPressed = false;

    /** Controls visuals */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 17) ctrlPressed = true;
        if (e.getKeyCode() == 16) shiftPressed = true;
        if (e.getKeyChar() == 'b') drawBoundingBox = !drawBoundingBox;
        if (e.getKeyChar() == ' ') {
            dots = new float[dotNum];
            for (int i = 0; i < dots.length; i++) {
                dots[i] = i * (arcLength[arcLength.length - 1] / dotNum);
            }
            playAnimation = !playAnimation;
        }
    }

    /** Resets animations */
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 17) ctrlPressed = false;
        if (e.getKeyCode() == 16) shiftPressed = false;
    }

    ControlPoint dragPoint;
    /** Sets dragPoint to closest control point in range on mouse press */
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == 3) {
            drawLines = !drawLines;
        }
        if (e.getButton() == 1) {
            for (ControlPoint point : controlPoints.values()) {
                if (point.pos.distance(screen.screenToNormal(e.getX(), e.getY())) <= controlPointRadius) {
                    dragPoint = point;
                }
            }
        }
    }

    /** Resets dragPoint */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == 1) {
            dragPoint = null;
        }
    }

    /** Drags control point if dragPoint (Controlled by mousePressed() and mouseReleased()) isn't null */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragPoint != null) {
            dragPoint.pos = screen.screenToNormal(e.getX(), e.getY());
            bindControlPointToEdge();
            if (playAnimation) {
                dots = new float[dotNum];
                for (int i = 0; i < dots.length; i++) {
                    dots[i] = i * (arcLength[arcLength.length - 1] / dotNum);
                }
            }
        }
    }

    /** Controls animation variables */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (ctrlPressed) {
            if (playAnimation) {
                if (Math.signum(e.getUnitsToScroll()) == -1) animationSpeed -= 10;
                if (Math.signum(e.getUnitsToScroll()) == 1) animationSpeed += 10;
                if (animationSpeed <= 0) animationSpeed = 1;
                if (animationSpeed > 700) animationSpeed = 700;
            }
        } else if (shiftPressed) {
            if (playAnimation) {
                if (Math.signum(e.getUnitsToScroll()) == -1) dotNum += 2;
                if (Math.signum(e.getUnitsToScroll()) == 1) dotNum -= 2;
                if (dotNum <= 0) dotNum = 1;
                if (dotNum > 35) dotNum = 35;
                dots = new float[dotNum];
                for (int i = 0; i < dots.length; i++) {
                    dots[i] = i * (arcLength[arcLength.length - 1] / dotNum);
                }
            }
        }
    }

    /** Resizes window on next render */
    @Override
    public void componentResized(ComponentEvent e) {
        super.componentResized(e);
        screen = new Screen(width, height);
    }
}
