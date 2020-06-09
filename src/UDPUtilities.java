/**
 * Rob Black
 * 4/23/2020
 */

import java.io.*;
import java.net.*;

/**
 * Additional Utilities
 */
public class UDPUtilities {
    public static Object receiveObject(DatagramSocket datagramSocket){
        try {
            byte[] recvBuffer = new byte[10000];
            DatagramPacket datagramPacket = new DatagramPacket(recvBuffer,recvBuffer.length);
            datagramSocket.receive(datagramPacket);
            ByteArrayInputStream byteArrayInputSteam = new ByteArrayInputStream(recvBuffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputSteam);
            return objectInputStream.readObject();
        } catch (SocketTimeoutException e){
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendObject(DatagramSocket datagramSocket, InetAddress address, int port, Serializable object){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.flush();
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();

            byte[] buffer = byteArrayOutputStream.toByteArray();

            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,address,port);
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert a byte[] to char[] for testing purposes. Used as a hash
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
