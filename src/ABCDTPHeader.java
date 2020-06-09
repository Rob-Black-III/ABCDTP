/**
 * Rob Black
 * 4/23/2020
 */

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * ABCDTPHeader is the first packet sent over to the server,
 * specifying how much data it should expect.
 */
public class ABCDTPHeader implements Serializable {
    private int fileSizeInBytes;
    private int numBetterPackets;
    private String filename;

    public ABCDTPHeader(int fileSizeInBytes, int numBetterPackets, String filename, InetSocketAddress self){
        this.fileSizeInBytes = fileSizeInBytes;
        this.numBetterPackets = numBetterPackets;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public int getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public int getNumBetterPackets() {
        return numBetterPackets;
    }
}
