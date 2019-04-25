package com.andrewofarm.galaxies.world;

import android.graphics.Color;
import android.opengl.Matrix;
import android.util.Log;

import com.andrewofarm.galaxies.android.util.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Andrew on 1/2/17.
 */

public class Universe {

    List<Body> blackHoles = new ArrayList<>();
    List<Body> stars = new ArrayList<>();
//    List<Integer> toRemove = new ArrayList<>();

    private final Random gen = new Random();

    public static final int MAX_STARS = 100000;

    private static final float GRAVITY = 1.0f;

    private static final int MIN_GALAXIES = 2;
    private static final int MAX_GALAXIES = 2;
    private static final float MIN_GALAXY_RADIUS = 50.0f;
    private static final float MAX_GALAXY_RADIUS = 30.0f;
    private static final float MIN_GALAXY_SPEED = 0.0f;
    private static final float MAX_GALAXY_SPEED = 0.3f;
    private static final float STARS_PER_GALAXY_AREA = 0.5f;
    private static final float MAX_START_POSITION = 50.0f;
    private static final float EVENT_HORIZON_RADIUS = 5.0f;
    private static final float EVENT_HORIZON_RADIUS_SQUARED =
            EVENT_HORIZON_RADIUS * EVENT_HORIZON_RADIUS;
    private static final float MIN_STAR_RADIUS = 1.0f;
    private static final float MAX_STAR_RADIUS = 4.0f;


    public void initStars() {
        int numGalaxies = gen.nextInt(MAX_GALAXIES - MIN_GALAXIES + 1) + MIN_GALAXIES;
        for (int i = 0; i < numGalaxies; i++) {
            //generate galaxy
            Vector3f galaxyPosition = new Vector3f(
                    gen.nextFloat() * MAX_START_POSITION,
                    gen.nextFloat() * MAX_START_POSITION,
                    gen.nextFloat() * MAX_START_POSITION);
            float galaxySpeed = genRadius(MIN_GALAXY_SPEED, MAX_GALAXY_SPEED);
            float galaxyRadius = genFloat(MIN_GALAXY_RADIUS, MAX_GALAXY_RADIUS);
            int numStars = (int) (galaxyRadius * galaxyRadius * Math.PI * STARS_PER_GALAXY_AREA);

            //randomize galaxy direction
            float[] directionRotationMatrix = new float[16];
            Matrix.setIdentityM(directionRotationMatrix, 0);
            Matrix.rotateM(directionRotationMatrix, 0, genAngleDegrees(), 1f, 0f, 0f);
            Matrix.rotateM(directionRotationMatrix, 0, genAngleDegrees(), 0f, 1f, 0f);
            Matrix.rotateM(directionRotationMatrix, 0, genAngleDegrees(), 0f, 0f, 1f);
            float[] galaxyVelocityCoordinates = {galaxySpeed, 0f, 0f, 1f};
            Matrix.multiplyMV(galaxyVelocityCoordinates, 0,
                    directionRotationMatrix, 0, galaxyVelocityCoordinates, 0);
            Vector3f galaxyVelocity = new Vector3f(galaxyVelocityCoordinates);

            //randomize galaxy orientation
            float[] positionRotationMatrix = new float[16];
            Matrix.setIdentityM(positionRotationMatrix, 0);
            Matrix.rotateM(positionRotationMatrix, 0, genAngleDegrees(), 1f, 0f, 0f);
            Matrix.rotateM(positionRotationMatrix, 0, genAngleDegrees(), 0f, 1f, 0f);
            Matrix.rotateM(positionRotationMatrix, 0, genAngleDegrees(), 0f, 0f, 1f);

            //create black hole
            Body blackHole = new Body(galaxyPosition, galaxyVelocity, EVENT_HORIZON_RADIUS, 0);
            blackHoles.add(blackHole);

            //generate stars in galaxy
            for (int j = 0; j < numStars; j++) {

                //generate star position
                float distanceFromCenter = gen.nextFloat() * galaxyRadius;
                float starAngle = genAngle();
                float maxVerticalVariance = (float) (1 / (1 + Math.pow(
                        distanceFromCenter / galaxyRadius * 3.0f, 2)) * .1 * galaxyRadius);
                float verticalVariance = genFloat(-maxVerticalVariance, maxVerticalVariance);
                Vector3f starPosition = new Vector3f(
                        (float) (distanceFromCenter * Math.cos(starAngle)),
                        (float) (distanceFromCenter * Math.sin(starAngle)),
                        verticalVariance);

                //calculate star velocity
                float dist3D = (float) Math.sqrt(distanceFromCenter * distanceFromCenter +
                        verticalVariance * verticalVariance);
                float speed = (float) Math.sqrt(GRAVITY / dist3D);
                float direction = (float) (starAngle + 0.5 * Math.PI);
                Vector3f starVelocity = new Vector3f(
                        (float) (speed * Math.cos(direction)),
                        (float) (speed * Math.sin(direction)),
                        0f);

                //adjust to galaxy orientation
                float[] starPositionCoordinates = starPosition.toArray4();
                float[] starVelocityCoordinates = starVelocity.toArray4();
                Matrix.multiplyMV(starPositionCoordinates, 0,
                        positionRotationMatrix, 0, starPositionCoordinates, 0);
                Matrix.multiplyMV(starVelocityCoordinates, 0,
                        positionRotationMatrix, 0, starVelocityCoordinates, 0);
                starPosition.set(starPositionCoordinates);
                starVelocity.set(starVelocityCoordinates);

                //adjust to galaxy position
                starPosition.add(galaxyPosition);
                starVelocity.add(galaxyVelocity);

                //generate star size
                float starRadius = genFloat(MIN_STAR_RADIUS, MAX_STAR_RADIUS);
                
                //calculate star color
                int color = getStarColorByDistance(distanceFromCenter / galaxyRadius);

                Body star = new Body(starPosition, starVelocity, starRadius, color);
                stars.add(star);
            }
        }
        Log.v("initStars", "Universe genesis complete. " + getStarCount() + " stars created.");
    }

    public void step() {
        //accelerate black holes
        Vector3f a = new Vector3f();
        for (Body bh : blackHoles) {
            a.setZero();
            for (Body bh2 : blackHoles) {
                if (bh2 != bh) {
                    a.add(gacc(bh, bh2));
                }
            }
            bh.accelerate(a);
        }

        //accelerate stars
        for (Body star : stars) {
            a.setZero();
            for (Body bh : blackHoles) {
                a.add(gacc(star, bh));
            }
            star.accelerate(a);
        }

        //move black holes
        for (Body bh : blackHoles) {
            bh.move();
        }

        //move stars
        for (Body star : stars) {
            star.move();
        }

//        //move stars
//        Vector3f dist = new Vector3f();
//        for (int i = 0; i < stars.size(); i++) {
//            Body star = stars.get(i);
//            star.move();
//
//            //suck stars into black holes, never to be seen again
//            for (Body bh : blackHoles) {
//                dist.set(bh.getPos());
//                dist.sub(star.getPos());
//                if (dist.lengthSquared() < EVENT_HORIZON_RADIUS_SQUARED) {
//                    toRemove.add(i);
//                    break;
//                }
//            }
//        }
//
//        //remove stars in toRemove list
//        //start from the end of the list, so each removal will not
//        //shift the indices of the other elements to be removed
//        for (int i = toRemove.size() - 1; i >= 0; i--) {
//            stars.remove(toRemove.get(i));
//        }
    }

    /** gravitational acceleration */
    Vector3f gacc(Body b1, Body b2) {
        Vector3f dist = new Vector3f(b2.getPos());
        dist.sub(b1.getPos());
        float magnitude = GRAVITY / dist.lengthSquared();
        dist.setLength(magnitude);
        return dist;
    }

    public int getStarCount() {
        return stars.size();
    }

    public void writeStarData(FloatBuffer vertexBuffer) {
        vertexBuffer.position(0);
        for (Body star : stars) {
            Vector3f pos = star.getPos();
            int color = star.getColor();

            vertexBuffer.put(pos.x);
            vertexBuffer.put(pos.y);
            vertexBuffer.put(pos.z);

            vertexBuffer.put(Color.red(color) / 255f);
            vertexBuffer.put(Color.green(color) / 255f);
            vertexBuffer.put(Color.blue(color) / 255f);

            vertexBuffer.put(star.getRadius());
        }
    }
    
    private int getStarColorByDistance(float normalizedDistance) {
        float red, green, blue;

        if (normalizedDistance < .5) {
            red = 1;
            if (normalizedDistance < .25) green = .9f + .2f * normalizedDistance;
            else green = 1;
            blue = .5f + normalizedDistance;
        } else {
            red = 1.4f - .8f * normalizedDistance;
            green = 1.2f - .4f * normalizedDistance;
            blue = 1;
        }

        return Color.rgb((int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

    private float genFloat(float min, float max) {
        return gen.nextFloat() * (max - min) + min;
    }

    private float genRadius(float min, float max) {
        return (float) (1 - Math.pow((1 - gen.nextFloat()), 2)) * (max - min) + min;
    }

    private float genAngle() {
        return (float) (gen.nextFloat() * 2f * Math.PI);
    }

    private float genAngleDegrees() {
        return gen.nextFloat() * 360f;
    }
}
