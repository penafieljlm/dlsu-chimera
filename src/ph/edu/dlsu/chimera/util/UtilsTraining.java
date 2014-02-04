/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.edu.dlsu.chimera.util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import ph.edu.dlsu.chimera.core.TrainingResult;
import ph.edu.dlsu.chimera.core.model.SubModel;
import ph.edu.dlsu.chimera.core.PduAtomic;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
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

    public static Object[] getCoreInstance(PduAtomic packet, Instances instances) {
        ArrayList<Object> instance = new ArrayList<Object>();
        Tcp tcp = packet.packet.getHeader(new Tcp());
        Udp udp = packet.packet.getHeader(new Udp());
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[0]) != null) {
            instance.add(packet.getProtocolName());
        }
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[1]) != null) {
            instance.add(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        }
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[2]) != null) {
            instance.add(((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 3600) + (Calendar.getInstance().get(Calendar.MINUTE) * 60) + (Calendar.getInstance().get(Calendar.SECOND) * 1)));
        }
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[3]) != null) {
            instance.add(packet.packet.size());
        }
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[4]) != null) {
            instance.add(((tcp == null) ? -1 : tcp.destination()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[5]) != null) {
            instance.add(((udp == null) ? -1 : udp.destination()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CORE_HEADERS[6]) != null) {
            instance.add(((tcp == null) ? -1 : tcp.flags()));
        }
        return instance.toArray(new Object[0]);
    }

    public static Object[] getCoreInstance(Object[] instance, Instances instances) {
        int coreHeadersCt = UtilsTraining.CORE_HEADERS.length;
        if (instances != null) {
            coreHeadersCt = 0;
            for (String h : UtilsTraining.CORE_HEADERS) {
                if (instances.attribute(h) != null) {
                    coreHeadersCt++;
                }
            }
        }
        Object[] subinst = new Object[coreHeadersCt];
        System.arraycopy(instance, 0, subinst, 0, subinst.length);
        return subinst;
    }

    public static Object[] getConnectionInstance(PduAtomic packet, Instances instances) {
        ArrayList<Object> instance = new ArrayList<Object>();
        Connection conn = packet.getConnection();
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[0]) != null) {
            instance.add(((conn == null) ? -1 : conn.ingressLastEncounterDeltaNs()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[1]) != null) {
            instance.add(((conn == null) ? -1 : conn.egressLastEncounterDeltaNs()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[2]) != null) {
            instance.add(((conn == null) ? -1 : conn.ingressEncounters()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[3]) != null) {
            instance.add(((conn == null) ? -1 : conn.egressEncounters()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[4]) != null) {
            instance.add(((conn == null) ? -1 : conn.ingressTotalSize()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[5]) != null) {
            instance.add(((conn == null) ? -1 : conn.egressTotalSize()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[6]) != null) {
            instance.add(((conn == null) ? -1 : conn.ingressAverageSize()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[7]) != null) {
            instance.add(((conn == null) ? -1 : conn.egressAverageSize()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[8]) != null) {
            instance.add(((conn == null) ? -1 : conn.ingressRatePerSec()));
        }
        if (instances == null || instances.attribute(UtilsTraining.CONN_HEADERS[9]) != null) {
            instance.add(((conn == null) ? -1 : conn.egressRatePerSec()));
        }
        return instance.toArray(new Object[0]);
    }

    public static Object[] getConnectionInstance(Object[] instance, Instances instances) {
        int coreHeadersCt = UtilsTraining.CORE_HEADERS.length;
        if (instances != null) {
            coreHeadersCt = 0;
            for (String h : UtilsTraining.CORE_HEADERS) {
                if (instances.attribute(h) != null) {
                    coreHeadersCt++;
                }
            }
        }
        int connHeadersCt = UtilsTraining.CONN_HEADERS.length;
        if (instances != null) {
            connHeadersCt = 0;
            for (String h : UtilsTraining.CONN_HEADERS) {
                if (instances.attribute(h) != null) {
                    connHeadersCt++;
                }
            }
        }
        Object[] subinst = new Object[connHeadersCt];
        System.arraycopy(instance, coreHeadersCt, subinst, 0, subinst.length);
        return subinst;
    }

    public static String[] getCriteriaHeaders(Criteria criteria, Instances instances) {
        ArrayList<String> headers = new ArrayList<String>();
        String exp = criteria.expression.replaceAll(" ", "");
        if (instances == null || instances.attribute("exp(" + exp + ").enc_timed") != null) {
            headers.add("exp(" + exp + ").enc_timed");
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_count") != null) {
            headers.add("exp(" + exp + ").enc_count");
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_tsize") != null) {
            headers.add("exp(" + exp + ").enc_tsize");
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_asize") != null) {
            headers.add("exp(" + exp + ").enc_asize");
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_rateps") != null) {
            headers.add("exp(" + exp + ").enc_rateps");
        }
        return headers.toArray(new String[0]);
    }

    public static Object[] getCriteriaInstance(Criteria criteria, PduAtomic packet, Instances instances) {
        String exp = criteria.expression.replaceAll(" ", "");
        ArrayList<Object> instance = new ArrayList<Object>();
        Statistics crtstats = packet.getStatistics(criteria);
        if (instances == null || instances.attribute("exp(" + exp + ").enc_timed") != null) {
            instance.add(((crtstats == null) ? -1 : crtstats.getLastEncounterDeltaNs()));
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_count") != null) {
            instance.add(((crtstats == null) ? -1 : crtstats.getTotalEncounters()));
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_tsize") != null) {
            instance.add(((crtstats == null) ? -1 : crtstats.getTotalSize()));
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_asize") != null) {
            instance.add(((crtstats == null) ? -1 : crtstats.getAverageSize()));
        }
        if (instances == null || instances.attribute("exp(" + exp + ").enc_rateps") != null) {
            instance.add(((crtstats == null) ? -1 : crtstats.getTrafficRatePerSec()));
        }
        return instance.toArray(new Object[0]);
    }

    public static Object[] getCriteriaInstance(Criteria criteria, Object[] headers, Object[] instance, Instances instances) {
        if (headers.length != instance.length) {
            return null;
        }
        String[] _headers = UtilsTraining.getCriteriaHeaders(criteria, instances);
        Object[] subinst = new Object[_headers.length];
        for (int hCounter = 0; hCounter < _headers.length; hCounter++) {
            String _header = _headers[hCounter];
            Object _value = null;
            for (int locCounter = 0; locCounter < headers.length; locCounter++) {
                if (_header.equals(headers[locCounter])) {
                    _value = instance[locCounter];
                }
            }
            subinst[hCounter] = _value;
        }
        return subinst;
    }

    public static String[] getCriteriasHeaders(Criteria[] criterias, Instances instances) {
        ArrayList<String> headers = new ArrayList<String>();
        for (Criteria crt : criterias) {
            headers.addAll(Arrays.asList(UtilsTraining.getCriteriaHeaders(crt, instances)));
        }
        return headers.toArray(new String[0]);
    }

    public static Object[] getCriteriasInstance(Criteria[] criterias, PduAtomic packet, Instances instances) {
        ArrayList<Object> instance = new ArrayList<Object>();
        for (Criteria crt : criterias) {
            instance.addAll(Arrays.asList(UtilsTraining.getCriteriaInstance(crt, packet, instances)));
        }
        return instance.toArray(new Object[0]);
    }

    public static String[] getHeaders(Criteria[] criterias, Instances instances) {
        ArrayList<String> headers = new ArrayList<String>();
        headers.addAll(Arrays.asList(UtilsTraining.CORE_HEADERS));
        headers.addAll(Arrays.asList(UtilsTraining.CONN_HEADERS));
        for (Criteria crt : criterias) {
            headers.addAll(Arrays.asList(UtilsTraining.getCriteriaHeaders(crt, instances)));
        }
        headers.add(UtilsTraining.ATTK_HEADER);
        return headers.toArray(new String[0]);
    }

    public static Object[] getInstance(Criteria[] criterias, PduAtomic packet, boolean tagAsAttack, Instances instances) {
        ArrayList<Object> instance = new ArrayList<Object>();
        instance.addAll(Arrays.asList(UtilsTraining.getCoreInstance(packet, instances)));
        instance.addAll(Arrays.asList(UtilsTraining.getConnectionInstance(packet, instances)));
        for (Criteria crt : criterias) {
            instance.addAll(Arrays.asList(UtilsTraining.getCriteriaInstance(crt, packet, instances)));
        }
        instance.add("" + tagAsAttack);
        return instance.toArray(new Object[0]);
    }

    public static TrainingResult train(File trainingFile, String filter, boolean exclude) throws Exception {
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
        HashMap<Criteria, File> criteriaDataSet = new HashMap<Criteria, File>();
        int ct = 0;
        for (Criteria crt : criterias) {
            criteriaDataSet.put(crt, File.createTempFile("crt[" + ct + "]", ".trntmpcsv"));
            ct++;
        }
        //instance count per training set
        long connectionInstancesCount = 0;
        HashMap<Criteria, Long> criteriaInstancesCount = new HashMap<Criteria, Long>();
        for (Criteria crt : criterias) {
            criteriaInstancesCount.put(crt, new Long(0));
        }
        //open writers and write headers
        String[] attackHeader = {UtilsTraining.ATTK_HEADER};
        HashMap<Criteria, CSVWriter> criteriaDataSetWriter;
        CSVWriter connDataSetWriter = new CSVWriter(new FileWriter(connectionDataSet));
        connDataSetWriter.writeNext(UtilsArray.concat(UtilsTraining.CORE_HEADERS, UtilsTraining.CONN_HEADERS, attackHeader));
        criteriaDataSetWriter = new HashMap<Criteria, CSVWriter>();
        for (Criteria crt : criteriaDataSet.keySet()) {
            criteriaDataSetWriter.put(crt, new CSVWriter(new FileWriter(criteriaDataSet.get(crt))));
            criteriaDataSetWriter.get(crt).writeNext(UtilsArray.toCsv(UtilsArray.concat(UtilsTraining.CORE_HEADERS, UtilsTraining.getCriteriaHeaders(crt, null), attackHeader)));
        }
        String[] headers = reader.readNext();
        if (headers == null) {
            throw new Exception("Missing headers.");
        }
        String[] instance;
        while ((instance = reader.readNext()) != null) {
            //core
            Object[] core = UtilsTraining.getCoreInstance(instance, null);
            Object[] attack = {instance[instance.length - 1]};
            //connection
            Object[] conn_inst = UtilsTraining.getConnectionInstance(instance, null);
            if (!UtilsTraining.instanceIsNull(conn_inst)) {
                connectionInstancesCount++;
                conn_inst = UtilsArray.concat(core, conn_inst, attack);
                connDataSetWriter.writeNext(UtilsArray.toCsv(conn_inst));
                connDataSetWriter.flush();
            }
            //criteria
            for (Criteria crt : criterias) {
                Object[] crt_inst = UtilsTraining.getCriteriaInstance(crt, headers, instance, null);
                if (!UtilsTraining.instanceIsNull(crt_inst)) {
                    criteriaInstancesCount.put(crt, criteriaInstancesCount.get(crt) + 1);
                    crt_inst = UtilsArray.concat(core, crt_inst, attack);
                    criteriaDataSetWriter.get(crt).writeNext(UtilsArray.toCsv(crt_inst));
                    criteriaDataSetWriter.get(crt).flush();
                }
            }
        }
        connDataSetWriter.close();
        for (Criteria crt : criteriaDataSetWriter.keySet()) {
            criteriaDataSetWriter.get(crt).close();
        }
        //create subset data sources
        CSVLoader connCsvLoader = (connectionInstancesCount > 0) ? new CSVLoader() : null;
        if (connCsvLoader != null) {
            connCsvLoader.setSource(connectionDataSet);
        }
        DataSource connSource = (connCsvLoader != null) ? new DataSource(connCsvLoader) : null;
        HashMap<Criteria, DataSource> criteriaSource = new HashMap<Criteria, DataSource>();
        for (Criteria crt : criteriaDataSet.keySet()) {
            CSVLoader crtCsvLoader = new CSVLoader();
            crtCsvLoader.setSource(criteriaDataSet.get(crt));
            if (criteriaInstancesCount.get(crt) > 0) {
                criteriaSource.put(crt, new DataSource(crtCsvLoader));
            }
        }
        File tConnFile = new File("__conn.csv");
        UtilsFile.copyFile(connectionDataSet, tConnFile);
        ct = 0;
        for (Criteria crt : criteriaDataSet.keySet()) {
            File tCrtFile = new File("__crt[" + ct + "].csv");
            UtilsFile.copyFile(criteriaDataSet.get(crt), tCrtFile);
            ct++;
        }
        //create subset instances
        Instances connInstance = (connSource != null) ? connSource.getDataSet() : null;
        HashMap<Criteria, Instances> criteriaInstance = new HashMap<Criteria, Instances>();
        for (Criteria crt : criteriaSource.keySet()) {
            criteriaInstance.put(crt, criteriaSource.get(crt).getDataSet());
        }
        //set class attributes
        if (connInstance != null) {
            if (connInstance.numAttributes() > 2) {
                if (connInstance.classIndex() == -1) {
                    connInstance.setClassIndex(connInstance.numAttributes() - 1);
                }
            } else {
                throw new Exception("Connection data set must have at least one custom attribute, and one class attribute.");
            }
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
        if (connInstance != null) {
            if (connInstance.classAttribute().numValues() == 1) {
                throw new Exception("Connection data set must have at least two variations of values for the class attribute.");
            }
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
        J48 connTree = (connInstance != null) ? new J48() : null;
        if (connTree != null) {
            connTree.setOptions(options);
        }
        HashMap<Criteria, J48> criteriaTree = new HashMap<Criteria, J48>();
        for (Criteria crt : criteriaInstance.keySet()) {
            J48 _criteriaTree = new J48();
            _criteriaTree.setOptions(options);
            criteriaTree.put(crt, _criteriaTree);
        }
        //build trees
        if (connTree != null && connInstance != null) {
            try {
                boolean _exclude;
                do {
                    _exclude = false;
                    Enumeration attrs = connInstance.enumerateAttributes();
                    while (attrs.hasMoreElements()) {
                        Attribute attr = (Attribute) attrs.nextElement();
                        _exclude = exclude;
                        if (filter != null) {
                            _exclude = !(attr.name().matches(filter) ^ _exclude);
                        }
                        if (_exclude) {
                            connInstance.deleteAttributeAt(attr.index());
                            break;
                        }
                    }
                } while (_exclude);
                connTree.buildClassifier(connInstance);
            } catch (Exception ex) {
                throw new Exception("Cannot build classifier for connection tree.");
            }
        }
        //criteria trees
        for (Criteria crt : criteriaTree.keySet()) {
            try {
                boolean _exclude;
                do {
                    _exclude = false;
                    Enumeration attrs = criteriaInstance.get(crt).enumerateAttributes();
                    while (attrs.hasMoreElements()) {
                        Attribute attr = (Attribute) attrs.nextElement();
                        _exclude = exclude;
                        if (filter != null) {
                            _exclude = !(attr.name().matches(filter) ^ _exclude);
                        }
                        if (_exclude) {
                            criteriaInstance.get(crt).deleteAttributeAt(attr.index());
                            break;
                        }
                    }
                } while (_exclude);
                criteriaTree.get(crt).buildClassifier(criteriaInstance.get(crt));
            } catch (Exception ex) {
                throw new Exception("Cannot build classifier for criteria tree.");
            }
        }
        //derive iface name
        StringBuilder iface = new StringBuilder();
        for (String iface1 : ifaces) {
            if (!iface1.isEmpty()) {
                iface = iface.append(iface1);
            }
        }
        //create submodels
        SubModel connSubModel = null;
        if (connInstance != null) {
            FastVector connAttrs = new FastVector();
            Enumeration _connAttrs = connInstance.enumerateAttributes();
            while (_connAttrs.hasMoreElements()) {
                connAttrs.addElement(_connAttrs.nextElement());
            }
            //determine double value of attack classification
            double _attack = (connInstance.firstInstance().stringValue(connInstance.numAttributes() - 1).equals("" + true)) ? 0.0 : 1.0;
            connSubModel = new SubModel(connTree, connAttrs, _attack);
        }
        HashMap<Criteria, SubModel> criteriaSubModels = new HashMap<Criteria, SubModel>();
        for (Criteria crt : criteriaTree.keySet()) {
            FastVector attrs = new FastVector();
            Enumeration _attrs = criteriaInstance.get(crt).enumerateAttributes();
            while (_attrs.hasMoreElements()) {
                attrs.addElement(_attrs.nextElement());
            }
            //determine double value of attack classification
            double _attack = (criteriaInstance.get(crt).firstInstance().stringValue(criteriaInstance.get(crt).numAttributes() - 1).equals("" + true)) ? 0.0 : 1.0;
            SubModel sm = new SubModel(criteriaTree.get(crt), attrs, _attack);
            criteriaSubModels.put(crt, sm);
        }
        //return model
        return new TrainingResult(new ModelLive(iface.toString(), connSubModel, criteriaSubModels), connInstance, criteriaInstance);
    }

    public static boolean instanceIsNull(Object[] instance) {
        for (Object i : instance) {
            if (i instanceof Number) {
                if (((Number) (i)).doubleValue() != -1) {
                    return false;
                }
            } else {
                if (!i.equals("" + null)) {
                    return false;
                }
            }
        }
        return true;
    }
}
