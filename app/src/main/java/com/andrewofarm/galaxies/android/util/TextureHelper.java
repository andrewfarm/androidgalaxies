package com.andrewofarm.galaxies.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by Andrew on 12/29/16.
 */

public class TextureHelper {

    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceID, int minFilter, int magFilter,
                                  boolean useMipmaps) {
        //create texture object
        final int[] textureObjectIDs = new int[1];
        glGenTextures(1, textureObjectIDs, 0);

        //check for errors
        if (textureObjectIDs[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not generate a new OpenGL texture object.");
            }
            return 0;
        }

        //load and decode image resource
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceID, options);

        //check for errors
        if (bitmap == null) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "ResourceID " + resourceID + " could not be decoded.");
            }
            glDeleteTextures(1, textureObjectIDs, 0);
            return 0;
        }

        //bind texture to GL_TEXTURE_2D
        glBindTexture(GL_TEXTURE_2D, textureObjectIDs[0]);

        //specify texture filtering (scaling) methods
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

        //load texture data into OpenGL
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        if (useMipmaps) {
            //generate mipmaps
            glGenerateMipmap(GL_TEXTURE_2D);
        }

        //unbind texture from GL_TEXTURE_2D
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIDs[0];
    }
}
