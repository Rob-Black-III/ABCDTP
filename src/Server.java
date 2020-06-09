/**
 * Rob Black
 * 4/23/2020
 */
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spawns the receive thread
 */
public class Server {
    public static void main(String[] args) {

        if(args.length != 0){
            System.err.println("Usage: Server");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        ConcurrentHashMap<Integer,ABCDTP> sequenceNumbersToDataMap = new ConcurrentHashMap<>();

        executorService.submit(new ServerThread(sequenceNumbersToDataMap));
        executorService.shutdown();
        //executorService.submit(new ServerSendAcknowledgeThread());


    }
}
