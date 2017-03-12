/** ========================================================================= *
 * Copyright (C)  2017 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice OooDomino.java
 */
package com.ibm.issc.domino.oooservice;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Registration;
import lotus.domino.Session;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Retrieve an OOO information from Domino using the Registration
 * class and access to the Domino database of the given user
 *
 * @author stw
 */
public class OooDomino {

    private final Logger logger = Activator.getLogger(this.getClass().getName());

    public OooStatus retrieveOOO(final Session session, final String username) {
        final Monitor mon = MonitorFactory.start("OooDomino#retrieveOOO");
        Monitor monRegistration = null;
        Monitor monDBOpen = null;
        boolean isMonRegistration = false;
        boolean isMonDBOpen = false;
        Database db = null;
        Registration registration = null;
        Document doc = null;

        // The OOO Status we return back - can have an error property
        final OooStatus ooStatus = new OooStatus(username);

        try {
            registration = session.createRegistration();

            final StringBuffer mailserver = new StringBuffer();
            final StringBuffer mailfile = new StringBuffer();
            final StringBuffer maildomain = new StringBuffer();
            final StringBuffer mailsystem = new StringBuffer();
            @SuppressWarnings("rawtypes")
            final Vector profile = new Vector();
            monRegistration = MonitorFactory.start("OooService#retrieveOOO#retrieveRegistration");
            isMonRegistration = true;
            registration.getUserInfo(username, mailserver, mailfile, maildomain, mailsystem, profile);
            monRegistration.stop();
            isMonRegistration = false;

            // Opening might fail depending on server trust and access control
            // However Domino will usually return a "dead" db object anyway
            boolean successfullDBOpen = true;
            monDBOpen = MonitorFactory.start("OooService#retrieveOOO#OpenDB");
            isMonDBOpen = true; // To avoid duplicate hit count on stop
            try {
                db = session.getDatabase(mailserver.toString(), mailfile.toString());

                if (!db.isOpen()) {
                    db.open();
                }
            } catch (final NotesException dbFail) {
                successfullDBOpen = false;
                ooStatus.setError(dbFail.text);
            }
            monDBOpen.stop();
            isMonDBOpen = false;

            if (successfullDBOpen) {
                final boolean OOenabled = db.getOption(Database.DBOPT_OUTOFOFFICEENABLED);
                ooStatus.setEnabled(OOenabled);

                // Retrieve the message only if it is active
                if (OOenabled) {
                    try {
                        doc = db.getProfileDocument("outofofficeprofile", null);
                        if (doc != null) {
                            this.retrieveOOOParameters(doc, ooStatus);
                        } else {
                            ooStatus.setError("User didn't provide Out-of-Office information");
                        }
                    } catch (final NotesException profileFail) {
                        ooStatus.setError(profileFail.text);
                    }
                }
            }

        } catch (final NotesException e) {
            Utils.logError(this.logger, e);
            ooStatus.setError(e.text);
        }

        Utils.shred(doc, db, registration);

        // Ensure all monitors are down
        if ((monDBOpen != null) && isMonDBOpen) {
            monDBOpen.stop();
        }
        if ((monRegistration != null) && isMonRegistration) {
            monRegistration.stop();
        }
        mon.stop();

        return ooStatus;
    }

    /**
     * @param doc
     * @param ooStatus
     */
    @SuppressWarnings("rawtypes")
    private void retrieveOOOParameters(final Document doc, final OooStatus ooStatus) {
        if (doc == null) {
            ooStatus.setError("OOO Profile document missing");
            return;
        }
        final Monitor mon = MonitorFactory.start("OooService#retrieveOOOParameters");
        try {
            doc.setPreferJavaDates(true);
            final Vector firstDayOutVector = doc.getItemValue("dateFirstDayOut");
            if ((firstDayOutVector != null) && !firstDayOutVector.isEmpty()) {
                final Date fdoTime = (Date) firstDayOutVector.get(0);
                ooStatus.setFirstDayOut(fdoTime);
            }
            final Vector firstDayBackVector = doc.getItemValue("dateFirstDayBack");
            if ((firstDayBackVector != null) && !firstDayBackVector.isEmpty()) {
                final Date fdb = (Date) firstDayBackVector.get(0);
                ooStatus.setFirstDayBack(fdb);
            }
            ooStatus.setSubject(doc.getItemValueString("daysoutdisplay"));
            ooStatus.setBody(doc.getItemValueString("generalmessage"));

        } catch (final Exception e) {
            Utils.logError(this.logger, e);
            if (e instanceof NotesException) {
                ooStatus.setError(((NotesException) e).text);
            } else {
                ooStatus.setError(e.getMessage());
            }
        }

        mon.stop();
    }

}
