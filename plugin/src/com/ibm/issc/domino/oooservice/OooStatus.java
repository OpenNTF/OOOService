/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice OooStatus.java
 */
package com.ibm.issc.domino.oooservice;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Charsets;
import com.google.gson.stream.JsonWriter;

/**
 * @author stw
 */
public class OooStatus {

    // JSON/JavaScript compatible date format
    private final SimpleDateFormat sdf          = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final String           user;
    // Based on Task status
    private boolean                enabled      = false;
    private Date                   lastUpdate   = new Date();
    // Based on db.getOption(Database.DBOPT_OUTOFOFFICEENABLED)
    private boolean                ooDbOption   = false;
    private String                 firstDayOut  = null;
    private String                 firstDayBack = null;
    private String                 subject      = null;
    private String                 body         = null;
    private String                 error        = null;
    private String                 taskState    = null;

    public OooStatus(final String user) {
        this.user = user;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(final String body) {
        this.body = body;
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @param errorMsg
     */
    public void setError(final String errorMsg) {
        this.error = errorMsg;

    }

    /**
     * @param firstDayBack
     *            the firstDayBack to set
     */
    public void setFirstDayBack(final Date firstDateBack) {
        if (firstDateBack == null) {
            this.firstDayBack = null;
        } else {
            this.firstDayBack = this.sdf.format(firstDateBack);
        }
    }

    /**
     * @param firstDayOut
     *            the firstDayOut to set
     */
    public void setFirstDayOut(final Date firstDate) {
        if (firstDate == null) {
            this.firstDayOut = null;
        } else {
            this.firstDayOut = this.sdf.format(firstDate);
        }
    }

    /**
     * @param oOoption
     */
    public void setOODBOption(boolean oOoption) {
        this.ooDbOption = oOoption;

    }

    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * @param taskState
     *            the taskState to set
     */
    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }

    public String toJSON(final boolean debug) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final JsonWriter json = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8)));

            json.setHtmlSafe(true);
            json.beginObject();
            json.name("user").value(this.user);
            json.name("enabled").value(this.enabled);
            if (this.error != null) {
                json.name("error").value(this.error);
            }
            if (this.enabled) {
                json.name("out").value(this.firstDayOut);
                json.name("in").value(this.firstDayBack);
                json.name("subject").value(this.nullNA(this.subject));
                json.name("body").value(this.nullNA(this.body));
            }
            json.name("lastUpdate").value(this.sdf.format(this.lastUpdate));
            if (debug) {
                json.name("taskstate").value(this.taskState);
                json.name("ooDBStatus").value(this.ooDbOption);
            }
            json.endObject();
            json.flush();
            json.close();
        } catch (final IOException e) {
            Utils.logError(null, e);
        }

        return out.toString();
    }

    @Override
    public String toString() {
        return this.toJSON(false);
    }

    private String nullNA(final String input) {
        return (input == null ? "n/a" : input);
    }
}
