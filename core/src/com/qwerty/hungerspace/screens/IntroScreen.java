package com.qwerty.hungerspace.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.qwerty.hungerspace.HungerSpaceMain;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.qwerty.hungerspace.HungerSpaceMain.SCREEN_HEIGHT;
import static com.qwerty.hungerspace.HungerSpaceMain.SCREEN_WIDTH;
import static com.qwerty.hungerspace.HungerSpaceMain.font;
import static java.awt.Color.PINK;
import static java.awt.SystemColor.text;

/**
 * This screen is used to represent the ending of a play session after the user has either lost, won
 * or quit the session.
 *
 */
public class IntroScreen extends AbstractScreen {
    private float timeElapsed = 0;

    private float lineHeight;
    private static final float margin = 20;

    private Music music;

    FrameBuffer fbo;
    TextureRegion fboTr;
    Sprite fboSp;
    ShaderProgram shaderProgram;

    public IntroScreen(HungerSpaceMain game) {
        super(game);

        lineHeight = font.getLineHeight();

        fbo = FrameBuffer.createFrameBuffer(Pixmap.Format.RGB565, SCREEN_WIDTH, SCREEN_HEIGHT, false);
        fboTr = new TextureRegion(fbo.getColorBufferTexture());
        fboSp = new Sprite(fboTr);
        fboSp.flip(false, true);

        String vs = "attribute vec4 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "\n" +
                "uniform mat4 u_projTrans;\n" +
                "\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "\n" +
                "void main() {\n" +
                "    v_color = a_color;\n" +
                "    v_texCoords = a_texCoord0;\n" +
                "    gl_Position = u_projTrans * a_position;\n" +
                "}\n";

        String fs = "#ifdef GL_ES\n" +
                "    precision mediump float;\n" +
                "#endif\n" +
                "\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "uniform mat4 u_projTrans;\n" +
                "\n" +
                "void main() {\n" +
                "        const float x1 = 1;\n" +
                "        const float y1 = 0;\n" +
                "        const float x2 = 0.3f;\n" +
                "        const float y2 = 1;\n" +
                "        float xScale = x1 + (v_texCoords.y - y1) / ((y2 - y1) / (x2 - x1));\n" +
                "        float x = (v_texCoords.x - 0.5f) / xScale + 0.5f;\n" +
                "        vec3 color = texture2D(u_texture, vec2(x, v_texCoords.y)).rgb;\n" +
                "\n" +
                "        gl_FragColor = vec4(color, 1.0);\n" +
                "}";

        shaderProgram = new ShaderProgram(vs, fs);
    }

    @Override
    public void update(float delta) {
        if (timeElapsed == 0) {
            music = Gdx.audio.newMusic(Gdx.files.internal("music/starWars.mp3"));

            music.play();
        }

        timeElapsed += delta;

        if (timeElapsed >= 42) {
            screensManager.pushScreen(game.screens.get("menu"));

            music.stop();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        fbo.begin();

        GL20 gl = Gdx.graphics.getGL20();
        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL_COLOR_BUFFER_BIT);

        if (timeElapsed < 7) {
            float y = (SCREEN_HEIGHT - 2*lineHeight - margin) / 2 + 3*margin;

            List<String> lines = new ArrayList<String>();
            lines.add("A long time ago, in a galaxy");
            lines.add("far, far away...");

            font.setColor(1, 1, 1, timeElapsed < 1? timeElapsed: timeElapsed > 6? (7 - timeElapsed) / 2: 1);

            GlyphLayout layout = new GlyphLayout(font, "");

            batch.begin();
            for (String line : lines) {
                layout.setText(font, line);
                final float fontX = (SCREEN_WIDTH - layout.width) / 2;
                font.draw(batch, layout, fontX, y);

                y -= lineHeight + margin;
            }
            batch.end();
        } else if (timeElapsed > 8 && timeElapsed < 15) {
            font.setColor(1, 1, 1, 1);

            float y = (SCREEN_HEIGHT - 2*lineHeight - margin) / 2 + 3*margin;

            List<String> lines = new ArrayList<String>();
            lines.add("HUNGER");
            lines.add("SPACE");

            font.getData().setScale(timeElapsed < 10? 4 / (timeElapsed - 8): timeElapsed > 13? (15 - timeElapsed): 2);
            GlyphLayout layout = new GlyphLayout(font, "");

            batch.begin();
            for (String line : lines) {
                layout.setText(font, line);
                final float fontX = (SCREEN_WIDTH - layout.width) / 2;
                font.draw(batch, layout, fontX, y);

                y -= lineHeight + margin;
            }
            batch.end();
        } else {
            font.getData().setScale(1);

            float y = -margin + (timeElapsed - 15) * 40;

            float[] vert = fboSp.getVertices();
            vert[5] += 20; // top-left vertex x co-ordinate
            vert[10] -= 20; // top-right vertex x co-ordinate

            List<String> lines = new ArrayList<String>();
            lines.add("HONGER SPACE*");
            lines.add("*So hungry that I can't even spell right.");
            lines.add("");
            lines.add("It is a period of civil war. Rebel spaceships have been captured,");
            lines.add("and are forced to fight till to the death. Only two ships remain,");
            lines.add("with no poisonous berries in sight to save their lives.");

            GlyphLayout layout = new GlyphLayout(font, "");

            batch.begin();
            for (String line : lines) {
                layout.setText(font, line);
                final float fontX = (SCREEN_WIDTH - layout.width) / 2;
                font.draw(batch, layout, fontX, y);

                y -= lineHeight + margin;
            }
            batch.end();

            if (timeElapsed > 37) {
                music.setVolume((42 - timeElapsed) / 5);
            }
        }

        fbo.end();

        batch.begin();
        if (timeElapsed > 15) {
            batch.setShader(shaderProgram);
        }
        batch.draw(fboSp, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.setShader(null);
        batch.end();
    }

    @Override
    public void dispose() {
        music.dispose();
    }
}
