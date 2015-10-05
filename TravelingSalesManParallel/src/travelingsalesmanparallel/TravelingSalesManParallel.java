package travelingsalesmanparallel;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * The main class of the program. Runs the entire thing and allows threads to
 * update the current best path distance and configuration.
 * 
 * @author Eric Stones
 */
public class TravelingSalesManParallel {
    
    private static float currentShortestPath = 0;
    private static int[][] currentShortestConfiguration;
    private static final int processors = Runtime.getRuntime()
            .availableProcessors();
    //private static final int processors = 10;
    /**
     * Takes the input distances, creates the priority queue, puts the first
     * node in the queue, creates the threads, and runs them. It then waits for
     * all threads to finish before reporting on the best node that they found.
     * 
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args)  throws FileNotFoundException, 
            IOException{
        File f = new File("src/TSPInput.txt");
        int size;
        int[][] distances;
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            size = Integer.parseInt(in.readLine());
            distances = new int[size][size];
            for(int i = 0; i < size; i++){
                String[] text = in.readLine().split(" ");
                for(int j = 0; j < size; j++){
                    distances[i][j] = Integer.parseInt(text[j]);
                }
            }
        }
        currentShortestConfiguration = new int[size][size];
        int[][] usedPaths = new int[size][size];
        for(int i = 0; i < size; i++){
            usedPaths[i][i] = -1;
            for(int j = 0; j < i; j++){
                usedPaths[i][j] = usedPaths[j][i] = 0;
            }
        }
        Node n = new Node(usedPaths, distances);
        PriorityQueue pq = new PriorityQueue(processors);
        pq.accessQueue(false, n);
        Runner[] threads = new Runner[processors];
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < processors; i++){
            threads[i] = new Runner(i);
        }
        for(int i = 0; i < processors; i++){
            threads[i].start();
        }
        for(int i = 0; i < processors; i++){
            try {
                threads[i].join();
            } catch (InterruptedException ex) {}
        }
        
        long time = System.currentTimeMillis() - startTime;
        System.out.println("All done!");
        System.out.println("The shortest path is " + currentShortestPath + 
                " units long.");
        System.out.println("It took " + time + 
                " miliseconds to find the answer.");
        System.out.println(processors + " thread(s) looked through " + 
                pq.getNodesExplored() + " nodes to find that answer.");
        int[] path = new int[size+1];
        path[0] = 0;
        int city = 0;
        int count = 1;
        do{
            for(int i = 0; i < size; i++){
                if(currentShortestConfiguration[city][i]==1){
                    path[count] = i;
                    currentShortestConfiguration[city][i] = 0;
                    currentShortestConfiguration[i][city] = 0;
                    count++;
                    city = i;
                    i = size;
                }
            }
        }while(city!=0);
        System.out.print("Path taken: ");
        for(int i = 0; i < size; i++){
            System.out.print(path[i] + " - ");
        }
        System.out.println(path[0]);
    }
    
    /**
     * Gives the current shortest path through all the cities if one had been
     * found. Else it returns what it was initialized at: 0.
     * 
     * @return The current shortest path through all cities or 0 if none has
     *         been found yet.
     */
    public static float getCurrentShortestPath(){
        return currentShortestPath;
    }

    /**
     * Updates the current shortest path distance and path configuration. Will
     * update if either the distance in the node is shorter than the one
     * currently held or if the one currently held hasn't been updated yet.
     * 
     * @param n The node that might have a better path than what is held.
     */
    public static synchronized void update(Node n){
        if(n.getMinDistance()<currentShortestPath||currentShortestPath==0){
            currentShortestPath = n.getMinDistance();
            for(int i = 0; i < n.getConnections().length; i++){
                System.arraycopy(n.getConnections()[i], 0, 
                        currentShortestConfiguration[i], 0, 
                        n.getConnections().length);
            }
        }
    }
}

/**
 * Takes a node, expands it, and puts the two resulting nodes in the queue. If a
 * node is complete, it attempts to update. If it is illegal, it just ignores
 * the node. This keeps happening until it has updated, or the node that it
 * removed has a greater distance than the held distance, provided that another
 * thread has updated it.
 * 
 * @author Eric
 */
class Runner extends Thread{
    private final int id;
    
    /**
     * Assigns the thread an id number.
     * @param x 
     */
    public Runner(int x){
        id = x;
        System.out.println("Thread " + id + " created.");
    }
    
    /**
     * Loops until none of the threads are working, or all nodes it will remove
     * will be larger than the already updated shortest distance.
     */
    @Override
    public void run(){
        System.out.println("Thread " + id + " starting.");
        boolean noneWorking = false;
        while(!noneWorking){
            Node n;
            n = PriorityQueue.accessQueue(true, null);
            
            if(n==null){
                PriorityQueue.setWorking(id, false);
                noneWorking = PriorityQueue.noneWorking();
            }else if(n.isComplete()){
                System.out.println("Thread " + id
                        + " found a possible solution!");
                if(n.getMinDistance()<
                        TravelingSalesManParallel.getCurrentShortestPath()||
                        TravelingSalesManParallel.getCurrentShortestPath()==0){
                    TravelingSalesManParallel.update(n);
                }
                PriorityQueue.setWorking(id, false);
            }else if(n.isLegal()&&(n.getMinDistance()<
                    TravelingSalesManParallel.getCurrentShortestPath()||
                    TravelingSalesManParallel.getCurrentShortestPath()==0)){
                PriorityQueue.setWorking(id, true);
                int[][] temp1 = new int[n.getConnections().length]
                        [n.getConnections().length];
                int[][] temp2 = new int[n.getConnections().length]
                        [n.getConnections().length];
                for(int i = 0; i < n.getConnections().length; i++){
                    System.arraycopy(n.getConnections()[i], 0, temp1[i], 0, 
                            temp1.length);
                    System.arraycopy(n.getConnections()[i], 0, temp2[i], 0, 
                            temp2.length);
                }
                for(int i = 0; i < temp1.length; i++){
                    for(int j = 0; j < i; j++){
                        if(temp1[i][j]==0){
                            temp1[i][j] = temp1[j][i] = 1;
                            temp2[i][j] = temp2[j][i] = -1;
                            i = temp1.length;
                            j = temp1.length;
                        }
                    }
                }
                Node n1 = new Node(temp1, n.getDistances());
                Node n2 = new Node(temp2, n.getDistances());
                if(n1.isLegal()&&(n1.getMinDistance()<
                        TravelingSalesManParallel.getCurrentShortestPath()||
                        TravelingSalesManParallel.getCurrentShortestPath()==0)){
                    PriorityQueue.accessQueue(false, n1);
                }
                if(n2.isLegal()&&(n2.getMinDistance()<
                        TravelingSalesManParallel.getCurrentShortestPath()||
                        TravelingSalesManParallel.getCurrentShortestPath()==0)){
                    PriorityQueue.accessQueue(false, n2);
                }
            }else if(n.getMinDistance()>
                    TravelingSalesManParallel.getCurrentShortestPath()&&
                    TravelingSalesManParallel.getCurrentShortestPath()!=0){
            }
        }
        System.out.println("Thread " + id + " exiting.");
    }
}

/**
 * Maintains a queue ordered by the lower bound of the nodes placed into it. The
 * ones with the smaller lower bound n the front. Also keeps track of which
 * threads are busy. Threads only become not busy when there are no more nodes
 * in the queue. Also keeps track of how many nodes were removed from the queue
 * for reporting purposes.
 * 
 * @author Eric
 */
class PriorityQueue {
    private static ArrayList<Node> pq;
    private static boolean[] working;
    private static int nodes;
    
    /**
     * Creates the queue and the array that keeps track of which threads are 
     * working.
     * 
     * @param p The number of threads working on the queue.
     */
    public PriorityQueue(int p){
        pq = new ArrayList<>();
        working = new boolean[p];
        for(int i = 0; i < p; i++){
            working[i] = true;
        }
        nodes = 0;
    }
    
    /**
     * Gives the total number of nodes that have been removed from the queue,
     * and therefore, expanded.
     * 
     * @return How many nodes have been removed from the queue.
     */
    public int getNodesExplored(){
        return nodes;
    }
    
    /**
     * Sets whether or not a thread is currently working.
     * 
     * @param i The id of the thread to be set.
     * @param b The value of whether or not the thread is working.
     */
    public static void setWorking(int i, boolean b){
        working[i] = b;
    }
    
    /**
     * Checks to see if no threads are currently working. Will only happen when
     * the queue is empty and all threads are trying to remove a node.
     * 
     * @return True if none are working. False if even one is still working.
     */
    public static boolean noneWorking(){
        boolean b = true;
        for(int i = 0; i < working.length; i++){
            if(working[i])
                b = false;
        }
        return b;
    }
    
    /**
     * The only public method to access the queue. It is synchronized to the
     * threads don't get in each others way. The boolean parameter determines if
     * the queue is going to be added to or removed from.
     * 
     * @param m Determines which action to take. True to remove the first node,
     *          false to add the given node.
     * @param n The node to be added. If m is true, the value doesn't matter.
     * @return If m is true, it returns the first node in the queue. If m is
     *         false, or there are no nodes null is returned.
     */
    public static synchronized Node accessQueue(boolean m, Node n){
        if(m){
            nodes++;
            return remove();
        }else{
            add(n);
            return null;
        }
    }
    
    /**
     * Gives the first node in the queue and removes that node. If the queue is
     * empty it returns null.
     * @return The first node in the queue or null.
     */
    private static Node remove(){
        Node n = null;
        if(!pq.isEmpty()){
            n = pq.remove(0);
            if(n.getMinDistance()>=
                    TravelingSalesManParallel.getCurrentShortestPath()&&
                    TravelingSalesManParallel.getCurrentShortestPath()!=0&&
                    !pq.isEmpty()){
                System.out.println("Pruning " + pq.size() + " nodes!");
                pq.clear();
                
            }
        }
        return n;
    }
    
    /**
     * If the queue is empty, it just places the node in the queue. If it isn't
     * empty, it finds the place where the node belongs and places it.
     * 
     * @param n The node to be added to the queue.
     */
    private static void add(Node n){
        if(pq.isEmpty()){
            pq.add(n);
        }else{
            //pq.add(n);
            binaryAdd(n, 0, pq.size()-1);
        }
    }
    
    /**
     * If it finds the base case, the node is added to the queue. Else it checks
     * the position halfway between start and end and decides where the node
     * should go in relation to that. Then it recursively calls itself until it
     * finds the right position.
     * 
     * @param n The node to be added to the queue.
     * @param start The earliest position being considered.
     * @param end The last position being considered.
     */
    private static void binaryAdd(Node n, int start, int end){
        if(end<start||end==start&&n.getMinDistance()<=
                pq.get(start).getMinDistance())
            pq.add(start, n);
        else if(end==start&&n.getMinDistance()>pq.get(start).getMinDistance())
            pq.add(start+1, n);
        else{
            int mid = (start+end)/2;
            if(n.getMinDistance()==pq.get(mid).getMinDistance())
                pq.add(mid, n);
            else if(n.getMinDistance()<pq.get(mid).getMinDistance())
                binaryAdd(n, start, mid-1);
            else
                binaryAdd(n, mid+1, end);
        }
    }
}

/**
 * Hold all relevant information regarding the path lower bound and path
 * configuration. Information is determined upon creation.
 * 
 * @author Eric
 */
class Node {
    private final boolean complete;
    private final boolean legal;
    private final float minDistance;
    private final boolean preLoop;
    private int[][] connections;
    private int[][] distances;
    
    /**
     * Copies the arrays that were given to it, and then determines the lower
     * bound, whether or not it has a premature loop, is complete, and is legal.
     * 
     * @param c The path configuration of the cities.
     * @param d The distances between the cities.
     */
    public Node(int[][] c, int[][] d){
        connections = new int[c.length][c.length];
        distances = new int[d.length][d.length];
        for(int i = 0; i < c.length; i++){
            System.arraycopy(c[i], 0, connections[i], 0, c.length);
            System.arraycopy(d[i], 0, distances[i], 0, d.length);
        }
        minDistance = computeMinDistance();
        preLoop = computePreLoop();
        complete = computeComplete();
        legal = computeLegal();
    }
    
    /**
     * Gives the array containing the distances between the cities.
     * 
     * @return Returns the array containing the distances between the cities.
     */
    public int[][] getDistances(){
        return distances;
    }
    
    /**
     * Gives the array containing the configuration of the current path.
     * 
     * @return Returns the array containing the configuration of the current path.
     */
    public int[][] getConnections(){
        return connections;
    }
    
    /**
     * Gives the lower bound of the current path.
     * 
     * @return Returns the lower bound of the current path, as given by the
     *         field minDistance.
     */
    public float getMinDistance(){
        return minDistance;
    }
    
    /**
     * Computes the minimum distance by finding the two shortest paths attached
     * to each city, but giving priority to paths that have been included in the
     * path configuration, then divides the whole thing by 2.
     * 
     * @return Returns the lower bound of the current path.
     */
    private float computeMinDistance(){
        float shortest = 0;
        for(int i = 0; i < distances.length; i++){
            float shortest1 = 0;
            float shortest2 = 0;
            boolean lock1 = false;
            boolean lock2 = false;
            for(int j = 0; j < distances.length; j++){
                if(connections[i][j]==1){
                    if(!lock1){
                        shortest2 = shortest1;
                        shortest1 = distances[i][j];
                        lock1 = true;
                    }else{
                        shortest2 = distances[i][j];
                        lock2 = true;
                    }
                }else if(connections[i][j]==0){
                    if((shortest1==0||distances[i][j]<shortest1)&&!lock1){
                        shortest2 = shortest1;
                        shortest1 = distances[i][j];
                    }else if((shortest2==0||distances[i][j]<shortest2)&&!lock2){
                        shortest2 = distances[i][j];
                    }
                }
            }
            shortest+=(shortest1+shortest2);
        }
        return shortest/2;
    }
    
    /**
     * Tells whether or not the current path is complete.
     * 
     * @return Returns the completeness of the path, as given by the field
     *         complete.
     */
    public boolean isComplete(){
        return complete;
    }
    
    /**
     * Determines if the current path is complete or not. If there is a
     * premature loop, it can't be complete. Else, if every city has exactly two
     * edges attached, it is complete.
     * 
     * @return Returns true if it is a complete path, false if it isn't.
     */
    private boolean computeComplete(){
        if(preLoop)
            return false;
        for(int i = 0; i < connections.length; i++){
            int count = 0;
            for(int j = 0; j < connections.length; j++){
                if(connections[i][j]==1){
                    count++;
                }
            }
            if(count!=2){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gives the legality of the current path.
     * 
     * @return Returns the legality of the current path, as given by the field
     *         legal.
     */
    public boolean isLegal(){
        return legal;
    }
    
    /**
     * Determines the legality of the current path. If there are too many or not
     * enough paths to a city, it isn't legal. Else it bases it's answer off of
     * whether or not there is a premature loop.
     * 
     * @return Returns true if it is a legal path, false if it isn't.
     */
    private boolean computeLegal(){
        for(int i = 0; i < connections.length; i++){
            int used = 0;
            int unsure = 0;
            for(int j = 0; j < connections.length; j++){
                if(connections[i][j]==1){
                    used++;
                }else if(connections[i][j]==0){
                    unsure++;
                }
            }
            if(used>2||used+unsure<2){
                return false;
            }
        }
        return !preLoop;
    }
    
    /**
     * Determines if the current path has a premature loop.
     * 
     * @return Returns true if there is a premature loop, false if there isn't.
     */
    private boolean computePreLoop(){
        int[][] usedPaths = new int[connections.length][connections.length];
        for(int i = 0; i < usedPaths.length; i++){
            System.arraycopy(connections[i], 0, usedPaths[i], 0, 
                    usedPaths.length);
        }
        
        for(int i = 0; i < usedPaths.length; i++){
            for(int j = 0; j < i; j++){
                if(usedPaths[i][j]==1){
                    int start = i;
                    int current = j;
                    usedPaths[i][j] = 0;
                    usedPaths[j][i] = 0;
                    int edges = 1;
                    
                    while(current!=start){
                        int temp = current;
                        for(int k = 0; k < usedPaths.length; k++){
                            if(usedPaths[current][k]==1){
                                usedPaths[current][k] = 0;
                                usedPaths[k][current] = 0;
                                current = k;
                                edges++;
                            }
                        }
                        if(temp==current){
                            break;
                        }
                    }
                    if(current==start&&edges!=usedPaths.length){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}