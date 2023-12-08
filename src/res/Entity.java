package res;

public class Entity extends BaseEntity {
    public Vector2 velocity, acceleration;

    public Entity(Vector2 _pos, Vector2 _velocity, Vector2 _acceleration) {
        super(_pos);
        velocity = _velocity;
        acceleration = _acceleration;
    }

    public Entity() {
        super();
        velocity = new Vector2();
        acceleration = new Vector2();
    }
}
