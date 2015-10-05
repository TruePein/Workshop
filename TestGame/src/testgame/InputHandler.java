/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testgame;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author Eric
 */
public class InputHandler implements KeyListener {

    boolean[] keys;
    boolean[] released;
    boolean[] hit;

    public InputHandler(Component c) {
        c.addKeyListener(this);
        keys = new boolean[256];
        released = new boolean[256];
        for (int i = 0; i < released.length; i++) {
            released[i] = true;
        }
        hit = new boolean[256];
    }

    public boolean isKeyDown(int keyCode) {
        if (keyCode > 0 && keyCode < 256) {
            return keys[keyCode];
        }
        return false;
    }

    public boolean isKeyHit(int keyCode) {
        if (keyCode >= 0 && keyCode < 256) {
            if (hit[keyCode]) {
                hit[keyCode] = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode > 0 && keyCode < 256) {
            keys[keyCode] = true;
            if (released[keyCode]) {
                released[keyCode] = false;
                hit[keyCode] = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode > 0 && keyCode < 256) {
            keys[keyCode] = false;
            released[keyCode] = true;
        }
    }

}
