package res;

public class Screen {
    public int width, height;

    public Screen(int _width, int _height) {
        width = _width;
        height = _height;
    }

    public Vector2 screenToNormal(Vector2 pos) {
        return new Vector2(pos.x - (float) width / 2, pos.y - (float) height / 2);
    }

    public Vector2 normalToScreen(Vector2 pos) {
        return new Vector2(pos.x + (float) width / 2, pos.y + (float) height / 2);
    }

    public Vector2 screenToNormal(float x, float y) {
        return new Vector2(x - (float) width / 2, y - (float) height / 2);
    }

    public Vector2 normalToScreen(float x, float y) {
        return new Vector2(x + (float) width / 2, y + (float) height / 2);
    }
}
