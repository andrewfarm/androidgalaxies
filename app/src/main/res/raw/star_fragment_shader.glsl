precision mediump float;

uniform sampler2D u_TextureUnit;

varying vec3 v_Color;

void main()
{
//    vec2 p = gl_PointCoord * 2.0 + vec2(-1.0, -1.0);
//    float alpha = 1.0 - (p.x * p.x + p.y * p.y);
//    gl_FragColor = v_Color * alpha;
    gl_FragColor = vec4(v_Color, 1.0) * texture2D(u_TextureUnit, gl_PointCoord);
}
