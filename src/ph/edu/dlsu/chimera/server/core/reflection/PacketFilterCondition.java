/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.edu.dlsu.chimera.server.core.reflection;

import java.math.BigInteger;
import org.jnetpcap.packet.PcapPacket;

/**
 *
 * @author John Lawrence M. Penafiel <penafieljlm@gmail.com>
 */
public final class PacketFilterCondition extends PacketFilter {

    public final PacketField subject;
    public final OperationsCompare operation;
    public final BigInteger value;

    //syntax: <field>=<value>
    public PacketFilterCondition(String expression) throws Exception {
        String[] e = expression.split("((==)|(!=)|(<=)|(>=)|(<)|(>))");
        if (e.length != 2) {
            throw new Exception("Parse Error: Syntax structure error in expression '" + expression + "'");
        }
        if (expression.contains("==")) {
            this.operation = OperationsCompare.Equals;
        } else if (expression.contains("!=")) {
            this.operation = OperationsCompare.NotEquals;
        } else if (expression.contains("<=")) {
            this.operation = OperationsCompare.LessThanOrEquals;
        } else if (expression.contains(">=")) {
            this.operation = OperationsCompare.GreaterThanOrEquals;
        } else if (expression.contains("<")) {
            this.operation = OperationsCompare.LessThan;
        } else if (expression.contains(">")) {
            this.operation = OperationsCompare.GreaterThan;
        } else {
            throw new Exception("Parse Error: Syntax structure error in expression '" + expression + "'");
        }
        this.subject = new PacketField(e[0].trim());
        ParsedValue _value = new ParsedValue(e[1].trim());
        this.value = _value.value;
    }

    @Override
    public boolean matches(PcapPacket pkt) {
        BigInteger val = this.subject.getFieldValue(pkt);
        if (val != null) {
            switch (this.operation) {
                case Equals:
                    return val.compareTo(this.value) == 0;
                case NotEquals:
                    return val.compareTo(this.value) != 0;
                case LessThanOrEquals:
                    return val.compareTo(this.value) <= 0;
                case GreaterThanOrEquals:
                    return val.compareTo(this.value) >= 0;
                case LessThan:
                    return val.compareTo(this.value) < 0;
                case GreaterThan:
                    return val.compareTo(this.value) > 0;
            }
        }
        return false;
    }
}
