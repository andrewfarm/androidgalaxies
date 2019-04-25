package com.andrewofarm.galaxies.android.programs;

import android.content.Context;
import static android.opengl.GLES20.*;

import com.andrewofarm.galaxies.R;

/**
 * Created by Andrew on 1/3/17.
 */

public class StarShaderProgram extends ShaderProgram {

    private static final String U_MATRIX = "u_Matrix";
    public final int uMatrixLocation;
    private static final String A_POSITION = "a_Position";
    public final int aPositionLocation;
    private static final String A_COLOR = "a_Color";
    public final int aColorLocation;
    private static final String A_RADIUS = "a_Radius";
    public final int aRadiusLocation;
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    public final int uTextureUnitLocation;

    public StarShaderProgram(Context context) {
        super(context, R.raw.star_vertex_shader, R.raw.star_fragment_shader);

        uMatrixLocation = glGetUniformLocation(programID, U_MATRIX);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aColorLocation = glGetAttribLocation(programID, A_COLOR);
        aRadiusLocation = glGetAttribLocation(programID, A_RADIUS);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
    }
}
