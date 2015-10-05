/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testgame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 *
 * @author Eric
 */
public class TestGame extends JFrame {

    int fps = 60;
    int windowWidth = 400;
    int windowHeight = 400;
    int x;
    final int MIN_X = 0;
    final int MAX_X = 379;
    final int START_X = (MIN_X + MAX_X) / 2;
    int y;
    final int MIN_Y = 0;
    final int MAX_Y = 379;
    final int START_Y = (MIN_Y + MAX_Y) / 2;
    BufferedImage backBuffer;
    Insets insets;
    InputHandler input;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestGame tg = new TestGame();
        tg.run();
        System.exit(0);
    }

    public void run() {
        initialize();
        boolean isRunning = true;
        while (isRunning) {
            long time = System.currentTimeMillis();

            update();
            draw();

            time = (1000 / fps) - (System.currentTimeMillis() - time);

            if (time > 0) {
                try {
                    Thread.sleep(time);
                } catch (Exception e) {
                }
            }
        }
        setVisible(false);
    }

    public void initialize() {
        setTitle("Test Game");
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        x = START_X;
        y = START_Y;
        insets = getInsets();
        setSize(insets.left + windowWidth + insets.right, insets.top + windowHeight + insets.bottom);
        backBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        input = new InputHandler(this);
    }

    public void update() {
        if (input.isKeyHit(KeyEvent.VK_DOWN)) {
            if (y < 375) {
                y += 5;
            } else {
                y = 379;
            }
        }
        if (input.isKeyDown(KeyEvent.VK_S)) {
            if (y < 379) {
                y++;
            }
        }
        if (input.isKeyHit(KeyEvent.VK_UP)) {
            if (y > 4) {
                y -= 5;
            } else {
                y = 0;
            }
        }
        if (input.isKeyDown(KeyEvent.VK_W)) {
            if (y > 0) {
                y--;
            }
        }
        if (input.isKeyHit(KeyEvent.VK_LEFT)) {
            if (x > 4) {
                x -= 5;
            } else {
                x = 0;
            }
        }
        if (input.isKeyDown(KeyEvent.VK_A)) {
            if (x > 0) {
                x--;
            }
        }
        if (input.isKeyHit(KeyEvent.VK_RIGHT)) {
            if (x < 375) {
                x += 5;
            } else {
                x = 379;
            }
        }
        if (input.isKeyDown(KeyEvent.VK_D)) {
            if (x < 379) {
                x++;
            }
        }
    }

    public void draw() {
        Graphics g = getGraphics();

        Graphics bbg = backBuffer.getGraphics();

        bbg.setColor(Color.WHITE);
        bbg.fillRect(0, 0, windowWidth, windowHeight);

        bbg.setColor(Color.BLACK);
        bbg.drawOval(x, y, 20, 20);

        g.drawImage(backBuffer, insets.left, insets.top, this);
    }
}
