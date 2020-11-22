package sctp;

public class ScType {

    // sc_type_node          ?
    static public int Node = 0x1;
    // sc_type_link          ?
    static public int Link = 0x2;
    // sc_type_edge_common       ?
    static public int EdgeCommon = 0x4;
    // sc_type_arc_common        ?
    static public int ArcCommon = 0x8;
    // sc_type_arc_access ScType::EdgeAccess  http://ostis-dev.github.io/sc-machine/cpp/el_types/
    static public int ArcAccess = 0x10;

    // sc_type_const
    static public int Const = 0x20;
    static public int Var = 0x40;

    // sc_type_arc_pos
    static public int ArcPos = 0x80;

    // sc_type_arc_neg      ?
    static public int ArcNeg = 0x100;

    // sc_type_arc_fuz      ?
    static public int ArcFuz = 0x200;

    // sc_type_arc_temp     ?
    static public int ArcTemp = 0x400;

    // sc_type_arc_perm
    static public int ArcPerm = 0x800;

    static public int NodeTuple = 0x80;
    static public int NodeStruct = 0x100;
    static public int NodeRole = 0x200;
    static public int NodeNoRole = 0x400;
    // sc_type_node_class     ????
    static public int NodeClass = 0x800;
    static public int NodeAbstract = 0x1000;
    static public int NodeMaterial = 0x2000;

    // (  ->  ) ScType::EdgeAccessConstPosPerm  http://ostis-dev.github.io/sc-machine/cpp/el_types/
    // sc_type_arc_pos_const_perm ???
    static public int ArcPosConstPerm = ArcAccess | Const | ArcPos | ArcPerm;

    // sc_type_arc_common | sc_type_const     =>
    static public int ArcCommonConst = ArcCommon | Const;

    // sc_type_node | sc_type_const | sc_type_node_class
    static public int NodeConstClass = Node | Const | NodeClass;

    final static public int SIZE_BYTES = 2;

    private int Value;

    public ScType() {
        Value = 0;
    }

    public ScType(int inValue) {
        Value = inValue;
    }

    public boolean isValid() {
        return (Value != 0);
    }

    public int getValue() {
        return Value;
    }

    public boolean isEqual(ScType inType) {
        return Value == inType.Value;
    }
}
