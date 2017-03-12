/** ========================================================================= *
 * Copyright (C)  2016, 2017 IBM Corporation ( http://www.ibm.com/ )          *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice StatisticCollector.java
 */
package com.ibm.issc.domino.oooservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import lotus.notes.addins.JavaServerAddin;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

/**
 * Runs the jamon statistic collection on a per minute base
 * Based upon JavaServerAddin services
 *
 * @author dab
 */
public class StatisticCollector extends JavaServerAddin {

    private static final String     STATS_PREFIX   = "OOO";

    private static final int        STATS_INTERVAL = 10;

    private Logger                  logger;

    // Counting
    private final Map<String, Long> classCalls     = new ConcurrentHashMap<String, Long>();

    public StatisticCollector() {

    }

    /**
     * Record a call to a class for the statistics
     * Returns a logger, so we can use it in the logger instantiation
     *
     * @param className
     */
    public Logger recordClassCall(final String className) {
        try {
            final Long calls = this.classCalls.containsKey(className) ? (this.classCalls.get(className) + 1L) : 1L;
            this.classCalls.put(className, calls);
        } catch (final Throwable t) {
            // If we get here we need to reset our long
            this.classCalls.put(className, 0L);
        }
        return Logger.getLogger(className);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void runNotes() {
        this.logger = Logger.getLogger(this.getClass().getName());

        MonitorComposite rootMon = null;
        Monitor[] monitors = null;

        while (this.addInRunning()) {
            // Give control back to the OS
            this.OSPreemptOccasionally();

            // Check if it is time to dump the jamon stats to Domino
            if (this.AddInHasSecondsElapsed(StatisticCollector.STATS_INTERVAL)) {

                try {
                    // Get the root JAMon monitor set
                    rootMon = MonitorFactory.getRootMonitor();
                    if (rootMon != null) {
                        monitors = rootMon.getMonitors();
                        if (monitors != null) {
                            // Dump the stats
                            for (final Monitor mon : monitors) {
                                this.statUpdateMonitor(mon);
                            }
                        }
                    }

                    // Dump the renderCalls
                    for (final Map.Entry<String, Long> entry : StatisticCollector.this.classCalls.entrySet()) {
                        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, entry.getKey(), entry.getValue());
                    }

                    // Wait for 100 ms
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    Utils.logError(this.logger, e);
                }
            }
        }
    }

    /**
     * Console output on startup
     */
    public void startOOOStats() {
        final String version = Activator.getVersion();
        this.AddInLogMessageText("IBM Domino OOO Query Service " + version + " starting...");
        this.start();
        this.AddInLogMessageText("IBM Domino OOO Query Service " + version + " at your service!");
    }

    /**
     * Console output on shutdown
     */
    public void stopOOOStats() {
        this.AddInLogMessageText("Shutting down IBM Domino OOO Query Service ...");
        this.stopAddin();
        MonitorFactory.getRootMonitor().disable();
        MonitorFactory.disable();
        this.AddInLogMessageText("IBM Domino OOO Query Service terminated");

    }

    /**
     * Build Domino Stat label
     *
     * @param jamon
     * @param type
     *            of stat
     */
    private String buildStatLabel(final Monitor mon, final String type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(mon.getLabel().replaceAll("#", "."));
        sb.append(".");
        sb.append(type);
        return sb.toString();
    }

    private synchronized void serverStatUpdate(final String Package, final String Statistic, final double value) {
        this.StatUpdate(Package, Statistic, JavaServerAddin.ST_UNIQUE, JavaServerAddin.VT_NUMBER, new Double(value));
    }

    /**
     * Write the stats to the Domino stats package
     *
     * @param jamon
     */
    private void statUpdateMonitor(final Monitor mon) {
        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, this.buildStatLabel(mon, "Avg"), mon.getAvg());
        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, this.buildStatLabel(mon, "Min"), mon.getMin());
        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, this.buildStatLabel(mon, "Max"), mon.getMax());
        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, this.buildStatLabel(mon, "Hits"), mon.getHits());
        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, this.buildStatLabel(mon, "MaxActive"), mon.getMaxActive());
        this.serverStatUpdate(StatisticCollector.STATS_PREFIX, this.buildStatLabel(mon, "StdDev"), mon.getStdDev());
    }
}
