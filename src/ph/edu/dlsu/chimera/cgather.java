/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.edu.dlsu.chimera;

import com.gremwell.jnetbridge.PcapPort;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.jnetpcap.PcapIf;
import ph.edu.dlsu.chimera.server.Config;
import ph.edu.dlsu.chimera.core.Criteria;
import ph.edu.dlsu.chimera.server.assembly.AssemblyGathering;
import ph.edu.dlsu.chimera.core.reflection.PacketFilter;
import ph.edu.dlsu.chimera.util.ToolsInterface;
import ph.edu.dlsu.chimera.util.ToolsParse;

/**
 *
 * @author John Lawrence M. Penafiel <penafieljlm@gmail.com>
 */
public class cgather {

    public static final String USAGE = ""
            + "\nCHIMERA Gather Command Usage (cgather)"
            + "\nDescription:"
            + "\n    The cgather command starts the CHIMERA's data gathering phase."
            + "\n    The training set used in the Training Phase is compiled in this phase."
            + "\n    This phase produces the said training set using the traffic captured."
            + "\n    Training sets produced are stored on .csv files."
            + "\nSyntax:"
            + "\n    cgather (((-<varname> <value>) | (/<flag>))[ ])*(((-<varname> <value>) | (/<flag>)))"
            + "\nParameters"
            + "\n    -output"
            + "\n        DESCRIPTION"
            + "\n            The output file name of the training set to be produced."
            + "\n        REQUIRED........ Yes"
            + "\n    -external"
            + "\n        DESCRIPTION"
            + "\n            The index of the external interface."
            + "\n            Refer to the output of the 'cifaces' command."
            + "\n        REQUIRED........ No"
            + "\n        DEFAULT VALUE... as specified in the 'chimera.config' file"
            + "\n    -internal"
            + "\n        DESCRIPTION"
            + "\n            The index of the internal interface."
            + "\n            Refer to the output of the 'cifaces' command."
            + "\n        REQUIRED........ No"
            + "\n        DEFAULT VALUE... as specified in the 'chimera.config' file"
            + "\n    -access"
            + "\n            A CHIMERA-JNetPcap Packet Filter Expression."
            + "\n            If provided, the following apply:"
            + "\n                If the /allow flag is set, the following apply:"
            + "\n                    Matching packets are included in the training set."
            + "\n                    Non matching packets are excluded from the training set."
            + "\n                If the /allow flag is not set, the following apply:"
            + "\n                    Matching packets are excluded from the training set."
            + "\n                    Non matching packets are included in the training set."
            + "\n            If not provided, the following apply:"
            + "\n                If the /allow flag is set, the following apply:"
            + "\n                    All packets are included in the training set."
            + "\n                If the /allow flag is not set, the following apply:"
            + "\n                    All packets are excluded from the training set."
            + "\n        REQUIRED........ No"
            + "\n        DEFAULT VALUE... N/A"
            + "\n    /allow"
            + "\n        DESCRIPTION"
            + "\n            If set, the following apply:"
            + "\n                If -access is provided, the following apply:"
            + "\n                    Packets matching -access are flagged as attacks."
            + "\n                    Packets not matching -access are flagged as normal."
            + "\n                If -access is not provided, the following apply:"
            + "\n                    All packets are flagged as attacks."
            + "\n            If not set, the following apply:"
            + "\n                If -access is provided, the following apply:"
            + "\n                    Packets matching -access are flagged as normal."
            + "\n                    Packets not matching -access are flagged as attacks."
            + "\n                If -access is not provided, the following apply:"
            + "\n                    All packets are flagged as normal."
            + "\n        REQUIRED........ No"
            + "\n        DEFAULT VALUE... N/A"
            + "\n    -training"
            + "\n        DESCRIPTION"
            + "\n            A CHIMERA-JNetPcap Packet Filter Expression."
            + "\n            If provided, the following apply:"
            + "\n                If the /attack flag is set, the following apply:"
            + "\n                    Matching packets are flagged as attacks."
            + "\n                    Non matching packets are flagged as normal."
            + "\n                If the /attack flag is not set, the following apply:"
            + "\n                    Matching packets are flagged as normal."
            + "\n                    Non matching packets are flagged as attacks."
            + "\n            If not provided, the following apply:"
            + "\n                If the /attack flag is set, the following apply:"
            + "\n                    All packets are flagged as attacks."
            + "\n                If the /attack flag is not set, the following apply:"
            + "\n                    All packets are flagged as normal."
            + "\n        REQUIRED........ No"
            + "\n        DEFAULT VALUE... N/A"
            + "\n    /attack"
            + "\n        DESCRIPTION"
            + "\n            If set, the following apply:"
            + "\n                If -training is provided, the following apply:"
            + "\n                    Packets matching -training are flagged as attacks."
            + "\n                    Packets not matching -training are flagged as normal."
            + "\n                If -training is not provided, the following apply:"
            + "\n                    All packets are flagged as attacks."
            + "\n            If not set, the following apply:"
            + "\n                If -training is provided, the following apply:"
            + "\n                    Packets matching -training are flagged as normal."
            + "\n                    Packets not matching -training are flagged as attacks."
            + "\n                If -training is not provided, the following apply:"
            + "\n                    All packets are flagged as normal."
            + "\n        REQUIRED........ No"
            + "\n        DEFAULT VALUE... N/A";

    public static void main(String[] args) {
        try {
            //help
            if (args.length == 1) {
                if (args[0].equals("/help")) {
                    System.out.println(cgather.USAGE);
                    return;
                }
            }

            //load config
            Config config = Config.loadConfig();

            //load criterias
            Criteria[] criterias = Criteria.loadCriterias();

            //parse args
            HashMap<String, String> _args = ToolsParse.parseArgs(args);

            //load dump file
            if (!_args.containsKey("-output")) {
                throw new Exception("The argument '-output' must be provided.");
            }
            File trainingDumpFile = new File(_args.get("-output"));

            //load interfaces
            int ifExternalIdx = -1;
            int ifInternalIdx = -1;
            try {
                if (_args.containsKey("-external")) {
                    ifExternalIdx = Integer.parseInt(_args.get("-external"));
                }
            } catch (Exception ex) {
                throw new Exception("The argument '-external' must provide a numerical value.");
            }
            try {
                if (_args.containsKey("-internal")) {
                    ifInternalIdx = Integer.parseInt(_args.get("-internal"));
                }
            } catch (Exception ex) {
                throw new Exception("The argument '-internal' must provide a numerical value.");
            }
            ArrayList<PcapIf> interfaces = ToolsInterface.getInterfaces();
            String ifExternalName = null;
            String ifInternalName = null;
            try {
                ifExternalName = (ifExternalIdx < 0) ? config.ifExternal : interfaces.get(ifExternalIdx).getName();
            } catch (Exception ex) {
                throw new Exception("Interface index '" + ifExternalIdx + "' is invalid.");
            }
            try {
                ifInternalName = (ifInternalIdx < 0) ? config.ifInternal : interfaces.get(ifInternalIdx).getName();
            } catch (Exception ex) {
                throw new Exception("Interface index '" + ifInternalIdx + "' is invalid.");
            }
            PcapPort ifExternalPort = null;
            PcapPort ifInternalPort = null;
            try {
                ifExternalPort = new PcapPort(ifExternalName);
            } catch (Exception ex) {
                throw new Exception("Cannot open external interface.");
            }
            try {
                ifInternalPort = new PcapPort(ifInternalName);
            } catch (Exception ex) {
                throw new Exception("Cannot open internal interface.");
            }

            //exclude filter
            PacketFilter accessFilter = null;
            if (_args.containsKey("-access")) {
                accessFilter = PacketFilter.parseExpression(_args.get("-access"));
            }

            //gather access flag
            boolean allowFiltered = false;
            if (_args.containsKey("/allow")) {
                allowFiltered = Boolean.parseBoolean(_args.get("/allow"));
            }

            //filter
            PacketFilter trainingFilter = null;
            if (_args.containsKey("-training")) {
                trainingFilter = PacketFilter.parseExpression(_args.get("-training"));
            }

            //gather attacks flag
            boolean tagFilteredAsAttacks = false;
            if (_args.containsKey("/attack")) {
                tagFilteredAsAttacks = Boolean.parseBoolean(_args.get("/attack"));
            }

            ifExternalPort.start();
            ifInternalPort.start();

            //prepare assembly
            AssemblyGathering assembly = null;
            assembly = new AssemblyGathering(config.controlPort,
                    ifExternalPort,
                    ifInternalPort,
                    criterias,
                    trainingDumpFile,
                    accessFilter,
                    allowFiltered,
                    trainingFilter,
                    tagFilteredAsAttacks,
                    config.statsTimeoutMs,
                    config.stateTimeoutMs);
            assembly.run();

            //end
            ifExternalPort.stop();
            ifInternalPort.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            System.out.println("Type 'cgather /help' to see usage.");
            return;
        }
    }
}
