package Bezier;

import res.Screen;

public class Main {
    public static void main(String[] args) {
        BezierWindow window = new BezierWindow(1920, 1080, 1.0 / 2.0, 90);
        window.init("Bezier");
    }
}
