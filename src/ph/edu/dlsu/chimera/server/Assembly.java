/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ph.edu.dlsu.chimera.server;

import java.util.ArrayList;
import java.util.HashMap;
import ph.edu.dlsu.chimera.server.admin.AdministrativeModule;
import ph.edu.dlsu.chimera.server.admin.UserBase;

/**
 * 
 * @author John Lawrence M. Penafiel <penafieljlm@gmail.com>
 */
public class Assembly {

    public final AdministrativeModule admin;
    public final UserBase users;
    public final PortProtocolMap portProtocolMap;

    /**
     * The assembly of current active modules.
     */
    private Deployment deployment;

    public Assembly(int port, PortProtocolMap portProtocolMap) {
        this.admin = new AdministrativeModule(this, port);
        this.users = new UserBase();
        this.portProtocolMap = portProtocolMap;
    }

    /**
     * Aborts the current deployment and starts a new one.
     * @param deployment
     */
    public void setDeployment(Deployment deployment) {
        if(this.deployment != null)
            this.deployment.kill();
        this.deployment = deployment;
        if(this.deployment != null)
            this.deployment.start();
    }

    /**
     * @return the current deployment; null if there is no current deployment
     */
    public Deployment getDeployment() {
        return this.deployment;
    }

    public void startAdmin() {
        this.admin.start();
    }

}
