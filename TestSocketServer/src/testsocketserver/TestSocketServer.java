/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testsocketserver;

import java.net.*;
import java.io.*;

/**
 *
 * @author stone
 */
public class TestSocketServer extends Thread {

    private ServerSocket serverSocket;

    public TestSocketServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
    }

    public void run() {
        boolean connected = false;
        while (!connected) {
            try {
                System.out.println("Waiting for client on port "
                        + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to "
                        + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());
                System.out.println(in.readUTF());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalAddress() + ".\nGoodbye!");
                server.close();
                connected = true;
            } catch (SocketTimeoutException ste) {
                System.out.println("Socket timed out!");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int port = 6067;
        try {
            Thread t = new TestSocketServer(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
