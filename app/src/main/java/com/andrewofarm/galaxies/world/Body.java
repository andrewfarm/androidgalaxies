package com.andrewofarm.galaxies.world;

import com.andrewofarm.galaxies.android.Constants;
import com.andrewofarm.galaxies.android.util.Vector3f;

/**
 * Created by Andrew on 1/2/17.
 */

public class Body {

    public static final int POSITION_COMPONENT_COUNT = 3;
    public static final int COLOR_COMPONENT_COUNT = 3;
    public static final int RADIUS_COMPONENT_COUNT = 1;

    public static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT + RADIUS_COMPONENT_COUNT;
    public static final int STRIDE = TOTAL_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;

    private Vector3f pos = new Vector3f();
    private Vector3f vel = new Vector3f();
    private float radius;
    private int color;

    public Body(Vector3f pos, Vector3f vel, float radius, int color) {
        setPos(pos);
        setVel(vel);
        setRadius(radius);
        setColor(color);
    }

    public void accelerate(Vector3f acc) {
        vel.add(acc);
    }

    public void move() {
        pos.add(vel);
    }

    public Vector3f getPos() {
        return pos;
    }

    public void setPos(Vector3f pos) {
        this.pos.set(pos);
    }

    public Vector3f getVel() {
        return vel;
    }

    public void setVel(Vector3f vel) {
        this.vel.set(vel);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
