/**
 * Rob Black
 * 4/23/2020
 */

/**
 * ABCDTPAck is meant for a better implementation in the future, where ackFlags are sent
 */
public class ABCDTPAck {
    private int seqeunceID;
    private boolean ackFlag;
    public ABCDTPAck(int sequenceId, boolean ackFlag){
        this.seqeunceID = sequenceId;
        this.ackFlag = ackFlag;
    }

    public int getSeqeunceID() {
        return seqeunceID;
    }

    public void setSeqeunceID(int seqeunceID) {
        this.seqeunceID = seqeunceID;
    }

    public boolean isAckFlag() {
        return ackFlag;
    }

    public void setAckFlag(boolean ackFlag) {
        this.ackFlag = ackFlag;
    }
}
