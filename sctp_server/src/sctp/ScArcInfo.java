package sctp;

class ScArcInfo {

    public ScAddr beginAddr;
    public ScAddr endAddr;

    public ScArcInfo(ScAddr inBeingAddr, ScAddr inEndAddr) {
        beginAddr = inBeingAddr;
        endAddr = inEndAddr;
    }
}