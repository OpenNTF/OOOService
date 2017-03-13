/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice OooCache.java
 */
package com.ibm.issc.domino.oooservice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Caching of lookup result in memory to avoid expensive calls
 * to the Domino server infrastructure
 *
 * @author stw
 */
public enum OooCache {
    INSTANCE;

    // Settings for Cache size and cache duration
    private final static int               CACHE_EXPIRATION_MINUTES = 30;
    private final static int               CACHE_SIZE               = 1000;

    private final Cache<String, OooStatus> statusCache              = CacheBuilder
            .newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_EXPIRATION_MINUTES,
                    TimeUnit.MINUTES).build();

    private final Logger                   logger                   = Logger.getLogger(this.getClass().getName());

    public void dropFromCache(final String username) {
        try {
            this.statusCache.invalidate(username);
        } catch (Exception e) {
            Utils.logError(this.logger, e);
        }
    }

    /**
     * Retrieves the OOO Status from Cache or from Domino if not available in Cache (or forced)
     *
     * @param username
     *            - the user to check
     * @param force
     *            - bypass cache
     * @param debug
     *            - include debug information
     * @return the OOO Status
     */
    public OooStatus get(final String username, final boolean force, final boolean debug) {

        final Monitor mon = MonitorFactory.start("OooCache#get");

        if (force) {
            this.dropFromCache(username);
        }

        OooStatus ooStatus = null;

        try {
            ooStatus = this.statusCache.get(username, new Callable<OooStatus>() {

                @Override
                public OooStatus call() throws Exception {

                    final Monitor mon2 = MonitorFactory.start("OooCache#cachemiss");
                    Session session = null;

                    // The OOO Status we return back - can have an error property
                    OooStatus ooStatus = null;
                    final OooDomino ooDomino = new OooDomino();

                    NotesThread.sinitThread();
                    try {
                        session = NotesFactory.createSession();
                        ooStatus = ooDomino.retrieveOOO(session, username, debug);
                    } catch (final NotesException e) {
                        Utils.logError(OooCache.this.logger, e);
                        if (ooStatus == null) {
                            ooStatus = new OooStatus(username);

                        }
                        ooStatus.setError(e.text);
                    }

                    Utils.shred(session);

                    // Ensure all monitors are down
                    NotesThread.stermThread();

                    mon2.stop();
                    return ooStatus;
                }

            });
        } catch (ExecutionException e) {
            Utils.logError(this.logger, e);
            if (ooStatus == null) {
                ooStatus = new OooStatus(username);
            }
            ooStatus.setError(e.getMessage());
        }
        mon.stop();
        return ooStatus;

    }
}
