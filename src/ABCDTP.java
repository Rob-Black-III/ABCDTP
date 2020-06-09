/**
 * Rob Black
 * 4/23/2020
 */
import java.io.Serializable;

/**
 * A Better Connectionless Data Transfer Protocol
 *
 * Basically a Struct containing a sequenceID to aid
 * in guaranteeing packet delivery and ordering
 */
public class ABCDTP implements Serializable {
    private int sequenceID;
    private byte[] data;

    public ABCDTP(int sequenceID, byte[] data){
        this.sequenceID = sequenceID;
        this.data = data;
    }

    public int getSequenceID() {
        return sequenceID;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Chunk #\t" + this.getSequenceID() + "\tSize:\t" + this.getData().length + "\tHashcode:\t" + UDPUtilities.bytesToHex(this.getData()).hashCode();
    }
}
