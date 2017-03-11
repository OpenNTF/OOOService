/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooserviceplugin Utils.java
 */
package com.ibm.issc.domino.oooserviceplugin;

import java.util.Collection;

import lotus.domino.Base;
import lotus.domino.NotesException;

/**
 * @author stw
 */
public class Utils {
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
