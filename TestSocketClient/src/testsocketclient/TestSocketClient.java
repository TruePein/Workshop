/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testsocketclient;

import java.net.*;
import java.io.*;

/**
 *
 * @author stone
 */
public class TestSocketClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String servername = "localhost";
        int port = 6067;
        try {
            System.out.println("Connecting to " + servername + " on port " + port);
            Socket client = new Socket(servername, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("Hello from" + client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println("Server says: " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
