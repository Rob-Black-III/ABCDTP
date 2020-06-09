/**
 * Rob Black
 * 4/23/2020
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for receiving the data and outputting to file.
 */
public class ServerThread implements Runnable{

    public static final int PORT = 5000;

    private ConcurrentHashMap<Integer,ABCDTP> sequenceNumbersToDataMap;

    public ServerThread(ConcurrentHashMap<Integer,ABCDTP> sequenceNumbersToDataMap){
        this.sequenceNumbersToDataMap = sequenceNumbersToDataMap;
    }

    @Override
    public void run() {
        System.out.println("Server Receive File Data Thread Started...");
        DatagramSocket datagramSocket = null;

        try {
            datagramSocket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        int fileSizeInBytes = 0;
        int numPacketsExpected = 0;
        String filename = "";

        // Receive the header data
        Object headerDataPacket = null;

        InetAddress clientAddress = null;
        int clientPort = 0;

        try {
            byte[] recvBuffer = new byte[1000];
            DatagramPacket datagramPacket = new DatagramPacket(recvBuffer,recvBuffer.length);
            datagramSocket.receive(datagramPacket);
            ByteArrayInputStream byteArrayInputSteam = new ByteArrayInputStream(recvBuffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputSteam);
            headerDataPacket = objectInputStream.readObject();

            clientAddress = datagramPacket.getAddress();
            clientPort = datagramPacket.getPort();
        } catch (SocketTimeoutException e){
            System.out.println("Timeout. Waiting...");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (headerDataPacket instanceof ABCDTPHeader){
            ABCDTPHeader headerData = (ABCDTPHeader)headerDataPacket;
            System.out.println("File Size (bytes): " + headerData.getFileSizeInBytes());
            System.out.println("Num Packets:       " + headerData.getNumBetterPackets());

            fileSizeInBytes = headerData.getFileSizeInBytes();
            numPacketsExpected = headerData.getNumBetterPackets();
            filename = headerData.getFilename();
        }

        // Now that we have the header, set the timeout for 2 seconds
        try {
            datagramSocket.setSoTimeout(2000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        boolean needToRequest = true;
        while(true){

            // Receive Object
            Object object = UDPUtilities.receiveObject(datagramSocket);
            if(object instanceof ABCDTP){
                ABCDTP betterPacket = (ABCDTP) object;
                System.out.println(betterPacket);

                // Add the object to the hash map
                sequenceNumbersToDataMap.put(betterPacket.getSequenceID(),betterPacket);
                needToRequest = true;
            }
            else if(object.equals(1)){
                // Send the reply back
                if(needToRequest){
                    ArrayList<Integer> idsToGet = new ArrayList<Integer>();

                    for(int i=0;i<numPacketsExpected;i++) {
                        if (!sequenceNumbersToDataMap.keySet().contains(i)) {
                            idsToGet.add(i);
                            System.out.println("Request Sent for Chunk # " + i);
                            UDPUtilities.sendObject(datagramSocket,clientAddress,clientPort,i);
                        }
                        else{
                            // Send ack of receipt

                        }
                    }

                    // Reached "steady-state" eventual consistency
                    if(idsToGet.size() == 0){
                        System.out.println("Total Entries: " + this.sequenceNumbersToDataMap.keySet().size());
                        break;
                    }

                    needToRequest = false;
                }
            }
            else{
                continue;
            }
        }

        // Write Data to File
        File file = new File("new_" + filename);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            for(Integer sequenceID : sequenceNumbersToDataMap.keySet()){
                fileOutputStream.write(sequenceNumbersToDataMap.get(sequenceID).getData());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
