/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wait.notifyexample;

/**
 *
 * @author Eric
 */
public class WaitNotifyTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Message msg = new Message("process it");
        Waiter waiter = new Waiter(msg);
        new Thread(waiter,"waiter").start();
         
        Waiter waiter1 = new Waiter(msg);
        new Thread(waiter1, "waiter1").start();
         
        Notifier notifier = new Notifier(msg);
        new Thread(notifier, "notifier").start();
        System.out.println("All the threads are started");
    }
    
}
