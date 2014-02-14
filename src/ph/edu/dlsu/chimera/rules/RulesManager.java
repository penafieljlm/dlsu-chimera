/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.edu.dlsu.chimera.rules;

import de.tbsol.iptablesjava.IpTables;
import de.tbsol.iptablesjava.rules.IpRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Nikkol
 */
public final class RulesManager {

    public static final String CHIMERA_CHAIN = "CHIMERA";
    public static final String FORWARD_CHAIN = "FORWARD";
    public static final String DROP_JUMP = "DROP";
    private final IpTables ipTables;
    private final List<RuleInfo> rulesMap;
    private boolean hasUncommitedChanges;
    private final CommitThread commitThread;

    public RulesManager() throws Exception {
        //create rulesmap and filter
        this.ipTables = new IpTables("filter");
        this.rulesMap = Collections.synchronizedList(new ArrayList<RuleInfo>());
        //clean chains
        this.ipTables.flushEntries(RulesManager.FORWARD_CHAIN);
        //create / clean master chain
        if (!this.ipTables.getAllChains().contains(RulesManager.CHIMERA_CHAIN)) {
            this.ipTables.createChain(RulesManager.CHIMERA_CHAIN);
        } else {
            this.ipTables.flushEntries(RulesManager.CHIMERA_CHAIN);
        }
        //create link to chimera chain
        IpRule toChimeraChain = new IpRule();
        toChimeraChain.setJump(RulesManager.CHIMERA_CHAIN);
        this.ipTables.appendEntry(RulesManager.FORWARD_CHAIN, toChimeraChain);
        this.commitThread = new CommitThread(this);
        this.commitThread.start();
        this.commit();
    }

    public synchronized boolean isTampered() throws Exception {
        return this.ipTables.getAllRules(RulesManager.CHIMERA_CHAIN).size() != this.rulesMap.size();
    }

    public synchronized boolean contains(Object id) {
        for (RuleInfo r : this.rulesMap) {
            if (r.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean append(Object id, IpRule rule) throws Exception {
        this.hasUncommitedChanges = true;
        if (!this.contains(id)) {
            this.ipTables.appendEntry(RulesManager.CHIMERA_CHAIN, rule);
            return this.rulesMap.add(new RuleInfo(id, System.currentTimeMillis()));
        }
        return false;
    }

    public synchronized RuleInfo remove(int index) throws Exception {
        this.hasUncommitedChanges = true;
        this.ipTables.deleteNumEntry(RulesManager.CHIMERA_CHAIN, index);
        return this.rulesMap.remove(index);
    }

    int i = 0;

    public synchronized void commit() {
        synchronized (this.commitThread) {
            this.commitThread.notify();
        }
    }

    public synchronized boolean hasUncommitedChanges() {
        return this.hasUncommitedChanges;
    }

    public synchronized void free() throws Exception {
        this.ipTables.flushEntries(RulesManager.FORWARD_CHAIN);
        if (this.ipTables.getAllChains().contains(RulesManager.CHIMERA_CHAIN)) {
            this.ipTables.flushEntries(RulesManager.CHIMERA_CHAIN);
        }
        this.commit();
        this.ipTables.free();
    }

    public ArrayList<RuleInfo> getRulesMap() {
        return new ArrayList<RuleInfo>(this.rulesMap);
    }

    private class CommitThread extends Thread {

        public final RulesManager manager;
        private boolean isRunning;

        public CommitThread(RulesManager manager) {
            this.manager = manager;
            this.isRunning = false;
        }

        @Override
        public synchronized void start() {
            this.isRunning = true;
            super.start();
        }

        public synchronized void kill() {
            this.isRunning = false;
        }

        @Override
        public synchronized void run() {
            while (this.isRunning) {
                try {
                    System.out.println("before");
                    this.wait();
                    System.out.println("after");
                    synchronized (this.manager) {
                        boolean ok;
                        do {
                            ok = true;
                            try {
                                this.manager.ipTables.commit();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                ok = false;
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception ex1) {
                                    ex1.printStackTrace();
                                }
                            }
                        } while (!ok);
                        this.manager.hasUncommitedChanges = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Yeah");
        }
    }
}
