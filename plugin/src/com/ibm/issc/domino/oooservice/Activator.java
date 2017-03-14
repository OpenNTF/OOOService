/** ========================================================================= *
 * Copyright (C)  2017 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== *
 */
package com.ibm.issc.domino.oooservice;

import java.util.logging.Logger;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin implements BundleActivator {

    // The shared instance
    private static Activator          plugin;
    private static StatisticCollector stats  = new StatisticCollector();
    private static String             version;

    private static final Logger       logger = Logger.getLogger("Activator");

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return Activator.plugin;
    }

    /**
     * Create a logger and record the creation in the statistic package
     *
     * @param loggerName
     *            Name of the logger
     * @return a logger
     */
    public static Logger getLogger(final String loggerName) {
        return Activator.getStats().recordClassCall(loggerName);
    }

    public static StatisticCollector getStats() {
        return Activator.stats;
    }

    public static String getVersion() {
        if (Activator.version == null) {
            try {
                Activator.version = Activator.plugin.getBundle().getHeaders().get("Bundle-Version").toString();
            } catch (final Exception e) {
                Utils.logError(Activator.logger, e);
                Activator.version = "0.0.0";
            }
        }
        return Activator.version;
    }

    public Activator() {
        // No Action needed
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        Activator.plugin = this;
        // Start Statistics Collector
        Activator.stats.startOOOStats();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        Activator.plugin = null;
        Activator.stats.stopOOOStats();
        super.stop(context);
    }

}
