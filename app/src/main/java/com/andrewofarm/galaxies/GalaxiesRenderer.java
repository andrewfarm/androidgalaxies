package com.andrewofarm.galaxies;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.andrewofarm.galaxies.android.Constants;
import com.andrewofarm.galaxies.android.programs.StarShaderProgram;
import com.andrewofarm.galaxies.android.util.*;
import com.andrewofarm.galaxies.world.Body;
import com.andrewofarm.galaxies.world.Universe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glGetUniformfv;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * Created by Andrew on 12/27/16.
 */

public class GalaxiesRenderer implements Renderer {

    private FloatBuffer vertexData;
    private Context context;
    private StarShaderProgram shaderProgram;
    private int texture;

    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];

    private Universe universe;

    private double viewAngle = 0;


    public GalaxiesRenderer(Context context) {
        this.context = context;

        vertexData = ByteBuffer
                .allocateDirect(Universe.MAX_STARS * Body.TOTAL_COMPONENT_COUNT *
                        Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        universe = new Universe();
        universe.initStars();
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        //set clear color
        glClearColor(0f, 0f, 0f, 0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        shaderProgram = new StarShaderProgram(context);

        texture = TextureHelper.loadTexture(context, R.drawable.particle_texture,
                GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, true);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);


        MatrixHelper.perspectiveM(projectionMatrix, 90, (float) width / (float) height, 10, 1000);


    }

    @Override
    public void onDrawFrame(GL10 glUnused) {

        //update universe
        universe.step();

        //move view
        Matrix.setLookAtM(viewMatrix, 0, 200 * (float) Math.sin(viewAngle), 0f, 200 * (float) Math.cos(viewAngle), 0f, 0f, 0f, 0f, 1f, 0f);
        viewAngle -= 0.005;
        viewAngle %= Math.PI * 2;
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        //clear screen
        glClear(GL_COLOR_BUFFER_BIT);

        vertexData.clear();
        universe.writeStarData(vertexData);

        shaderProgram.useProgram();

        int dataOffset = 0;

        vertexData.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aPositionLocation, Body.POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, Body.STRIDE, vertexData);
        glEnableVertexAttribArray(shaderProgram.aPositionLocation);
        dataOffset += Body.POSITION_COMPONENT_COUNT;

        vertexData.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aColorLocation, Body.COLOR_COMPONENT_COUNT,
                GL_FLOAT, false, Body.STRIDE, vertexData);
        glEnableVertexAttribArray(shaderProgram.aColorLocation);
        dataOffset += Body.COLOR_COMPONENT_COUNT;

        vertexData.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aRadiusLocation, Body.RADIUS_COMPONENT_COUNT,
                GL_FLOAT, false, Body.STRIDE, vertexData);
        glEnableVertexAttribArray(shaderProgram.aRadiusLocation);

        //set uniforms
        glUniformMatrix4fv(shaderProgram.uMatrixLocation, 1, false, viewProjectionMatrix, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(shaderProgram.uTextureUnitLocation, 0);

        glDrawArrays(GL_POINTS, 0, universe.getStarCount());
    }
}
