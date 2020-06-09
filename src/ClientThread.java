/**
 * Rob Black
 * 4/23/2020
 */

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read the file, and send the data over UDP
 * Will listen and resend requests if notified
 */
public class ClientThread implements Runnable {

    private static final int CHUNK_SIZE = 576;

    private InetAddress serverAddress;
    private String filename;
    private ConcurrentHashMap<Integer,ABCDTP> sequenceNumbersToDataMap;
    private DataInputStream dataInputStream;

    public ClientThread(InetAddress serverAddress, String filename, ConcurrentHashMap<Integer,ABCDTP> sequenceNumbersToDataMap) throws FileNotFoundException {
        this.serverAddress = serverAddress;
        this.filename = filename;
        this.sequenceNumbersToDataMap = sequenceNumbersToDataMap;

        this.dataInputStream = new DataInputStream(new FileInputStream(filename));

        // DEBUG
/*        System.out.println("DIS created from FIS of " + filename);
        try {
            System.out.println("File Size Remaining: " + this.dataInputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void run() {
        System.out.println("Client Send File Data Thread Started...");

        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Calculate number of packets we will need to send
        int fileSizeInBytes = 0;
        try {
            fileSizeInBytes = dataInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int numBetterPackets = fileSizeInBytes / CHUNK_SIZE;
        if(fileSizeInBytes % CHUNK_SIZE != 0){
            numBetterPackets++;
        }


        System.out.println("File Size (bytes): " + fileSizeInBytes);
        System.out.println("Num Packets:       " + numBetterPackets);
        try {
            System.out.println("From:              " + InetAddress.getLocalHost().getHostAddress() + ":");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Send Packet
        UDPUtilities.sendObject(datagramSocket,serverAddress, ServerThread.PORT,new ABCDTPHeader(fileSizeInBytes,numBetterPackets,filename,new InetSocketAddress(datagramSocket.getLocalAddress(),datagramSocket.getLocalPort())));

        int currentSequenceNumber = 0;
        while(true){
            int remainingBytesEstimate = 0;

            try {
                remainingBytesEstimate = dataInputStream.available();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get the dataChunk
            ABCDTP dataChunk;
            if(remainingBytesEstimate >= CHUNK_SIZE){
                dataChunk = getDataChunkFromFile(CHUNK_SIZE,currentSequenceNumber);
            }
            else if(remainingBytesEstimate < CHUNK_SIZE && remainingBytesEstimate > 0){
                dataChunk = getDataChunkFromFile(remainingBytesEstimate,currentSequenceNumber);
            }
            else{
                break;
            }

            // Send the Chunk
            sendFileChunk(datagramSocket,dataChunk);
            currentSequenceNumber++;
        }

        // Now that we have the header, set the timeout for 1 seconds
        try {
            datagramSocket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Resend Data request
        while(true){
            //int maxCurrentPacketReceivedID = Collections.max(sequenceNumbersToDataMap.keySet());
            Object idToResendObject = UDPUtilities.receiveObject(datagramSocket);

            if(idToResendObject.equals(1)){
                System.out.println("Total Entries: " + this.sequenceNumbersToDataMap.keySet().size());
                System.exit(0);
            }

            if(idToResendObject instanceof Integer){
                Integer id = (Integer)idToResendObject;
                if (sequenceNumbersToDataMap.keySet().contains(id.intValue())) {
                    System.out.println("Request Received to resend Chunk # " + id.intValue());
                    sendFileChunk(datagramSocket,sequenceNumbersToDataMap.get(id.intValue()));
                }
            }
        }
    }

    /**
     * Gets a data chunk from the file so it isn't in memory.
     * @param chunkSize Specified chunk size in bytes
     * @param currentSequenceNumber sequence number for use in ordering
     * @return new ABCDTP packet
     */
    public ABCDTP getDataChunkFromFile(int chunkSize, int currentSequenceNumber) {
        // Get and store 'chunkSize' bytes in 'data'
        byte[] data = new byte[chunkSize];
        try {
            dataInputStream.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add that data to the Concurrent HashMap for caching
        ABCDTP betterPacket = new ABCDTP(currentSequenceNumber,data);
        this.sequenceNumbersToDataMap.put(currentSequenceNumber,betterPacket);
        System.out.println(betterPacket);

        // Return the data
        return betterPacket;
    }

    /**
     * Send the ABCDTP packet to the server
     * @param datagramSocket
     * @param dataChunk
     */
    public void sendFileChunk(DatagramSocket datagramSocket, ABCDTP dataChunk){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);

            oos.flush();
            oos.writeObject(dataChunk);
            oos.flush();

            byte[] buffer = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,serverAddress, ServerThread.PORT);

            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
