/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.edu.dlsu.chimera.util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import ph.edu.dlsu.chimera.core.Connection;
import ph.edu.dlsu.chimera.core.criteria.Criteria;
import ph.edu.dlsu.chimera.core.model.ModelLive;
import ph.edu.dlsu.chimera.core.Statistics;
import ph.edu.dlsu.chimera.pdu.PduAtomic;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author John Lawrence M. Penafiel <penafieljlm@gmail.com>
 */
public abstract class UtilsTraining {

    public static String CLASSIFIER_OPTIONS = "-C 0.25 -M 2";
    public static final String[] CORE_HEADERS = {"protocol",
        "weekday",
        "timeofday",
        "pdu_size",
        "dest_tcp",
        "dest_udp",
        "flag_tcp"};
    public static final String[] CONN_HEADERS = {"conn.in_enc_timed",
        "conn.ou_enc_timed",
        "conn.in_enc_count",
        "conn.ou_enc_count",
        "conn.in_tsize",
        "conn.ou_tsize",
        "conn.in_asize",
        "conn.ou_asize",
        "conn.in_rateps",
        "conn.ou_rateps"};
    public static final String ATTK_HEADER = "attack";

    public static String[] getCoreInstance(PduAtomic packet) {
        ArrayList<String> instance = new ArrayList<>();
        Tcp tcp = packet.packet.getHeader(new Tcp());
        Udp udp = packet.packet.getHeader(new Udp());
        instance.add("" + packet.getProtocolName());
        instance.add("" + Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        instance.add("" + ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 3600) + (Calendar.getInstance().get(Calendar.MINUTE) * 60) + (Calendar.getInstance().get(Calendar.SECOND) * 1)));
        instance.add("" + packet.packet.size());
        instance.add("" + ((tcp == null) ? -1 : tcp.destination()));
        instance.add("" + ((udp == null) ? -1 : udp.destination()));
        instance.add("" + ((tcp == null) ? -1 : tcp.flags()));
        return instance.toArray(new String[0]);
    }

    public static String[] getCoreInstance(String[] instance) {
        String[] subinst = new String[UtilsTraining.CORE_HEADERS.length];
        System.arraycopy(instance, 0, subinst, 0, subinst.length);
        return subinst;
    }

    public static String[] getConnectionInstance(PduAtomic packet) {
        ArrayList<String> instance = new ArrayList<>();
        Connection conn = packet.getConnection();
        instance.add("" + ((conn == null) ? -1 : conn.ingressLastEncounterDeltaNs()));
        instance.add("" + ((conn == null) ? -1 : conn.egressLastEncounterDeltaNs()));
        instance.add("" + ((conn == null) ? -1 : conn.ingressEncounters()));
        instance.add("" + ((conn == null) ? -1 : conn.egressEncounters()));
        instance.add("" + ((conn == null) ? -1 : conn.ingressTotalSize()));
        instance.add("" + ((conn == null) ? -1 : conn.egressTotalSize()));
        instance.add("" + ((conn == null) ? -1 : conn.ingressAverageSize()));
        instance.add("" + ((conn == null) ? -1 : conn.egressAverageSize()));
        instance.add("" + ((conn == null) ? -1 : conn.ingressRatePerSec()));
        instance.add("" + ((conn == null) ? -1 : conn.egressRatePerSec()));
        return instance.toArray(new String[0]);
    }

    public static String[] getConnectionInstance(String[] instance) {
        String[] subinst = new String[UtilsTraining.CONN_HEADERS.length];
        System.arraycopy(instance, UtilsTraining.CORE_HEADERS.length, subinst, 0, subinst.length);
        return subinst;
    }

    public static String[] getCriteriaHeaders(Criteria criteria) {
        ArrayList<String> headers = new ArrayList<>();
        String exp = criteria.expression.replaceAll(" ", "");
        headers.add("exp(" + exp + ").enc_timed");
        headers.add("exp(" + exp + ").enc_count");
        headers.add("exp(" + exp + ").enc_tsize");
        headers.add("exp(" + exp + ").enc_asize");
        headers.add("exp(" + exp + ").enc_rateps");
        return headers.toArray(new String[0]);
    }

    public static String[] getCriteriaInstance(Criteria criteria, PduAtomic packet) {
        ArrayList<String> instance = new ArrayList<>();
        Statistics crtstats = packet.getStatistics(criteria);
        instance.add("" + ((crtstats == null) ? -1 : crtstats.getLastEncounterDeltaNs()));
        instance.add("" + ((crtstats == null) ? -1 : crtstats.getTotalEncounters()));
        instance.add("" + ((crtstats == null) ? -1 : crtstats.getTotalSize()));
        instance.add("" + ((crtstats == null) ? -1 : crtstats.getAverageSize()));
        instance.add("" + ((crtstats == null) ? -1 : crtstats.getTrafficRatePerSec()));
        return instance.toArray(new String[0]);
    }

    public static String[] getCriteriaInstance(Criteria criteria, String[] headers, String[] instance) {
        if (headers.length != instance.length) {
            return null;
        }
        String[] _headers = UtilsTraining.getCriteriaHeaders(criteria);
        String[] subinst = new String[_headers.length];
        for (int hCounter = 0; hCounter < _headers.length; hCounter++) {
            String _header = _headers[hCounter];
            String _value = null;
            for (int locCounter = 0; locCounter < headers.length; locCounter++) {
                if (_header.equals(headers[locCounter])) {
                    _value = instance[locCounter];
                }
            }
            subinst[hCounter] = _value;
        }
        return subinst;
    }

    public static String[] getCriteriasHeaders(Criteria[] criterias) {
        ArrayList<String> headers = new ArrayList<>();
        for (Criteria crt : criterias) {
            headers.addAll(Arrays.asList(UtilsTraining.getCriteriaHeaders(crt)));
        }
        return headers.toArray(new String[0]);
    }

    public static String[] getCriteriasInstance(Criteria[] criterias, PduAtomic packet) {
        ArrayList<String> instance = new ArrayList<>();
        for (Criteria crt : criterias) {
            instance.addAll(Arrays.asList(UtilsTraining.getCriteriaInstance(crt, packet)));
        }
        return instance.toArray(new String[0]);
    }

    public static String[] getHeaders(Criteria[] criterias) {
        ArrayList<String> headers = new ArrayList<>();
        headers.addAll(Arrays.asList(UtilsTraining.CORE_HEADERS));
        headers.addAll(Arrays.asList(UtilsTraining.CONN_HEADERS));
        for (Criteria crt : criterias) {
            headers.addAll(Arrays.asList(UtilsTraining.getCriteriaHeaders(crt)));
        }
        headers.add(UtilsTraining.ATTK_HEADER);
        return headers.toArray(new String[0]);
    }

    public static String[] getInstance(Criteria[] criterias, PduAtomic packet, boolean tagAsAttack) {
        ArrayList<String> instance = new ArrayList<>();
        instance.addAll(Arrays.asList(UtilsTraining.getCoreInstance(packet)));
        instance.addAll(Arrays.asList(UtilsTraining.getConnectionInstance(packet)));
        for (Criteria crt : criterias) {
            instance.addAll(Arrays.asList(UtilsTraining.getCriteriaInstance(crt, packet)));
        }
        instance.add("" + tagAsAttack);
        return instance.toArray(new String[0]);
    }

    public static ModelLive createModel(File trainingFile) throws Exception {
        //open training set file
        CSVReader reader = new CSVReader(new FileReader(trainingFile));
        //read interface
        String[] ifaces = reader.readNext();
        //read criterias
        String[] _criterias = reader.readNext();
        Criteria[] criterias = new Criteria[_criterias.length];
        for (int i = 0; i < criterias.length; i++) {
            criterias[i] = new Criteria(_criterias[i]);
        }
        //create training subsets files
        File connectionDataSet = File.createTempFile("connection", ".trntmpcsv");
        HashMap<Criteria, File> criteriaDataSet = new HashMap<>();
        int ct = 0;
        for (Criteria crt : criterias) {
            criteriaDataSet.put(crt, File.createTempFile("crt[" + ct + "]", ".trntmpcsv"));
            ct++;
        }
        //open writers and write headers
        String[] attackHeader = {UtilsTraining.ATTK_HEADER};
        HashMap<Criteria, CSVWriter> criteriaDataSetWriter;
        try (CSVWriter connDataSetWriter = new CSVWriter(new FileWriter(connectionDataSet))) {
            connDataSetWriter.writeNext(UtilsArray.concat(UtilsTraining.CORE_HEADERS, UtilsTraining.CONN_HEADERS, attackHeader));
            criteriaDataSetWriter = new HashMap<>();
            for (Criteria crt : criteriaDataSet.keySet()) {
                criteriaDataSetWriter.put(crt, new CSVWriter(new FileWriter(criteriaDataSet.get(crt))));
                criteriaDataSetWriter.get(crt).writeNext(UtilsArray.concat(UtilsTraining.CORE_HEADERS, UtilsTraining.getCriteriaHeaders(crt), attackHeader));
            }
            String[] headers = reader.readNext();
            if (headers == null) {
                throw new Exception("Missing headers.");
            }
            String[] instance;
            while ((instance = reader.readNext()) != null) {
                //core
                String[] core = UtilsTraining.getCoreInstance(instance);
                String[] attack = {instance[instance.length - 1]};
                //connection
                String[] conn_inst = UtilsTraining.getConnectionInstance(instance);
                if (!UtilsTraining.instanceIsNull(conn_inst)) {
                    conn_inst = UtilsArray.concat(core, conn_inst, attack);
                    connDataSetWriter.writeNext(conn_inst);
                    connDataSetWriter.flush();
                }
                //criteria
                for (Criteria crt : criterias) {
                    String[] crt_inst = UtilsTraining.getCriteriaInstance(crt, headers, instance);
                    if (!UtilsTraining.instanceIsNull(crt_inst)) {
                        crt_inst = UtilsArray.concat(core, crt_inst, attack);
                        criteriaDataSetWriter.get(crt).writeNext(crt_inst);
                        criteriaDataSetWriter.get(crt).flush();
                    }
                }
            }
        }
        for (Criteria crt : criteriaDataSetWriter.keySet()) {
            criteriaDataSetWriter.get(crt).close();
        }
        //create subset data sources
        CSVLoader connCsvLoader = new CSVLoader();
        connCsvLoader.setSource(connectionDataSet);
        DataSource connSource = new DataSource(connCsvLoader);
        HashMap<Criteria, DataSource> criteriaSource = new HashMap<>();
        for (Criteria crt : criteriaDataSet.keySet()) {
            CSVLoader crtCsvLoader = new CSVLoader();
            crtCsvLoader.setSource(criteriaDataSet.get(crt));
            criteriaSource.put(crt, new DataSource(crtCsvLoader));
        }
        // -- test --
        File tConnFile = new File("__conn.csv");
        UtilsFile.copyFile(connectionDataSet, tConnFile);
        ct = 0;
        for (Criteria crt : criteriaDataSet.keySet()) {
            File tCrtFile = new File("__crt[" + ct + "].csv");
            UtilsFile.copyFile(criteriaDataSet.get(crt), tCrtFile);
            ct++;
        }
        // -- test --
        //create subset instances
        Instances connInstance = connSource.getDataSet();
        HashMap<Criteria, Instances> criteriaInstance = new HashMap<>();
        for (Criteria crt : criteriaSource.keySet()) {
            criteriaInstance.put(crt, criteriaSource.get(crt).getDataSet());
        }
        //set class attributes
        if (connInstance.numAttributes() > 2) {
            if (connInstance.classIndex() == -1) {
                connInstance.setClassIndex(connInstance.numAttributes() - 1);
            }
        } else {
            throw new Exception("Connection data set must have at least one custom attribute, and one class attribute.");
        }
        for (Criteria crt : criteriaInstance.keySet()) {
            if (criteriaInstance.get(crt).numAttributes() > 2) {
                if (criteriaInstance.get(crt).classIndex() == -1) {
                    criteriaInstance.get(crt).setClassIndex(criteriaInstance.get(crt).numAttributes() - 1);
                }
            } else {
                throw new Exception("Criteria data set must have at least one custom attribute, and one class attribute.");
            }
        }
        if (connInstance.classAttribute().numValues() == 1) {
            throw new Exception("Connection data set must have at least two variations of values for the class attribute.");
        }
        for (Criteria crt : criteriaInstance.keySet()) {
            if (criteriaInstance.get(crt).numAttributes() == 1) {
                throw new Exception("Criteria data set must have at least two variations of values for the class attribute.");
            }
        }
        //set classifier options
        String[] options;
        try {
            options = weka.core.Utils.splitOptions(UtilsTraining.CLASSIFIER_OPTIONS);
        } catch (Exception ex) {
            throw new Exception("Classifier options corrupted.");
        }
        //create trees
        J48 connTree = new J48();
        connTree.setOptions(options);
        HashMap<Criteria, J48> criteriaTree = new HashMap<>();
        for (Criteria crt : criterias) {
            J48 _criteriaTree = new J48();
            _criteriaTree.setOptions(options);
            criteriaTree.put(crt, _criteriaTree);
        }
        //criteria trees
        ct = 0;
        for (Criteria crt : criteriaTree.keySet()) {
            try {
                ObjectOutputStream _oos = new ObjectOutputStream(new FileOutputStream("--crt[" + ct + "].tree"));
                criteriaTree.get(crt).buildClassifier(criteriaInstance.get(crt));
                _oos.writeObject(criteriaTree.get(crt));
                _oos.flush();
                _oos.close();
                ct++;
            } catch (Exception ex) {
                throw new Exception("Cannot build classifier for criteria tree.");
            }
        }
        //build trees
        try {
            ObjectOutputStream _oos = new ObjectOutputStream(new FileOutputStream("--conn.tree"));
            connTree.buildClassifier(connInstance);
            _oos.writeObject(connTree);
            _oos.flush();
            _oos.close();
        } catch (Exception ex) {
            throw new Exception("Cannot build classifier for connection tree.");
        }
        //return model
        return new ModelLive(ifaces[0], connTree, criteriaTree);
    }

    public static void testClassifier(J48 tree, Instances data) throws Exception {
        System.out.println("\nNumber of Leaves: " + tree.measureNumLeaves());
        System.out.println("\nSize of the Tree: " + tree.measureTreeSize());
        //decision start
        Evaluation eval = new Evaluation(data);
        double x[] = eval.evaluateModel(tree, data);
        System.out.println(eval.toSummaryString("\n== Evaluation on Training Set ==\n", false));

        System.out.println("Eval result");
        int oneCount = 0;
        int zeroCount = 0;
        for (double d : x) {
            if (d == 1.0) {
                oneCount++;
            }
            if (d == 0.0) {
                zeroCount++;
            }
            System.out.println(d);
        }
        System.out.println("ONE: " + oneCount);
        System.out.println("ZERO: " + zeroCount);
        System.out.println(tree.graph());
    }

    public static boolean instanceIsNull(String[] instance) {
        for (String i : instance) {
            if (!i.equals("" + -1)) {
                return false;
            }
        }
        return true;
    }
}
