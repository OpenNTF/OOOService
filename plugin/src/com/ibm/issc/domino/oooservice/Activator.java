/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
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
        return plugin;
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
        return stats;
    }

    public static String getVersion() {
        if (version == null) {
            try {
                version = plugin.getBundle().getHeaders().get("Bundle-Version").toString();
            } catch (Exception e) {
                Utils.logError(Activator.logger, e);
                version = "0.0.0";
            }
        }
        return version;
    }

    public Activator() {
        // No Action needed
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        // Start Statistics Collector
        stats.startOOOStats();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        Activator.stats.stopOOOStats();
        super.stop(context);
    }

}
