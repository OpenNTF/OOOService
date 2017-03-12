/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice Utils.java
 */
package com.ibm.issc.domino.oooservice;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import lotus.domino.Base;
import lotus.domino.NotesException;

/**
 * @author stw
 */
public class Utils {

    final private static Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * Shorthand to fix the stupid bug that NotesError doesn't return a message
     *
     * @param logger
     *            the logger to record to
     * @param e
     *            the exception to capture
     */
    public static void logError(final Logger logger, final Exception e) {
        final Logger realLogger = (logger == null) ? Utils.logger : logger;
        String errorMessage = (e instanceof NotesException) ? ((NotesException) e).text : e.getMessage();
        if (errorMessage == null) {
            errorMessage = "No error message " + e.getClass().getName();
        }
        realLogger.log(Level.SEVERE, errorMessage, e);
    }

    /**
     * Shorthand to fix the stupid bug that NotesError doesn't return a message
     * Logging warnings that
     *
     * @param logger
     *            the logger to record to
     * @param e
     *            the exception to capture
     */
    public static void logWarning(final Logger logger, final Exception e) {
        final Logger realLogger = (logger == null) ? Utils.logger : logger;
        String errorMessage = (e instanceof NotesException) ? ((NotesException) e).text : e.getMessage();
        if (errorMessage == null) {
            errorMessage = "No warrning message " + e.getClass().getName();
        }
        realLogger.log(Level.WARNING, errorMessage, e);
    }

    /**
     * Get rid of all Notes objects
     *
     * @param morituri
     */
    public static void shred(final Base... morituri) {

        // Protect against null
        if (morituri == null) {
            return;
        }

        for (Base obsoleteObject : morituri) {
            if (obsoleteObject != null) {
                try {
                    obsoleteObject.recycle();
                } catch (final NotesException e) {
                    // We don't care we want go get
                    // rid of it anyway
                } finally {
                    obsoleteObject = null;
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static void shred(final Collection<Object> morituri) {

        // Protect against null
        if (morituri == null) {
            return;
        }

        for (Object obsoleteObject : morituri) {
            if (obsoleteObject != null) {
                try {
                    if (obsoleteObject instanceof Base) {
                        ((Base) obsoleteObject).recycle();
                    } else if (obsoleteObject instanceof Collection) {
                        Utils.shred((Collection<Object>) obsoleteObject);
                    }
                } catch (final NotesException e) {
                    // We don't care we want go get
                    // rid of it anyway
                } finally {
                    obsoleteObject = null;
                }
            }
        }
    }

}
