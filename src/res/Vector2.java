package res;

public class Vector2 {

    public float x, y;

    public Vector2(float X, float Y) {
        x = X;
        y = Y;
    }

    public Vector2() {
        x = 0;
        y = 0;
    }

    public String toString() {
        return x + ", " + y;
    }

    public float getMagnitude() {
        return Math.abs(x) + Math.abs(y);
    }

    public float getXMagnitude() {
        return Math.abs(x);
    }

    public float getYMagnitude() {
        return Math.abs(y);
    }

    public Vector2 normalize() {
        return new Vector2(x != 0 ? x / Math.abs(x) : 0, y != 0 ? y / Math.abs(y) : 0);
    }

    public Vector2 add(float i) {
        return new Vector2(x + i, y + i);
    }

    public Vector2 subtract(float i) {
        return new Vector2(x - i, y - i);
    }

    public Vector2 multiply(float i) {
        return new Vector2(x * i, y * i);
    }

    public Vector2 multiply(Vector2 i) {
        return new Vector2(x * i.x, y * i.y);
    }

    public Vector2 divide(float i) {
        return new Vector2(i != 0 ? x / i : 0, i != 0 ? y / i : 0);
    }

    public Vector2 add(Vector2 o) {
        return new Vector2(x + o.x, y + o.y);
    }

    public Vector2 subtract(Vector2 o) {
        return new Vector2(x - o.x, y - o.y);
    }

    public float distance(Vector2 o) {
        return (float) Math.sqrt((o.x - x) * (o.x - x) + (o.y - y) * (o.y - y));
    }

    public float distanceX(Vector2 o) {
        return (float) Math.sqrt((o.x - x) * (o.x - x));
    }

    public float distanceY(Vector2 o) {
        return (float) Math.sqrt((o.y - y) * (o.y - y));
    }
}



