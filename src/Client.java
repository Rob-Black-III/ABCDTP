/**
 * Rob Black
 * 4/23/2020
 */

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client spawns the send thread.
 */
public class Client {
    public static void main(String[] args) {
        InetAddress serverAddress = null;

        if(args.length != 2){
            System.err.println("Usage: Client <SERVER_IP> <FILENAME.BIN>");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            serverAddress = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        ConcurrentHashMap<Integer,ABCDTP> sequenceNumbersToDataMap = new ConcurrentHashMap<>();

        try {
            executorService.submit(new ClientThread(serverAddress,args[1],sequenceNumbersToDataMap));
        } catch (FileNotFoundException e) {
            System.err.println("File not found. Check path and filename.");
            System.exit(1);
        }
        //executorService.submit(new ClientReceiveAcknowledgeThread(sequenceNumbersToDataMap));

        executorService.shutdown();


    }
}
