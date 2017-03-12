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
    private final boolean          enabled;
    private String                 firstDayOut  = null;
    private String                 firstDayBack = null;
    private String                 subject      = null;
    private String                 body         = null;

    public OooStatus(final String user, final boolean enabled) {
        this.user = user;
        this.enabled = enabled;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(final String body) {
        this.body = body;
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
     * @param subject
     *            the subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final JsonWriter json = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8)));

            json.setHtmlSafe(true);
            json.beginObject();
            json.name("user").value(this.user);
            json.name("enabled").value(this.enabled);
            if (this.enabled) {
                json.name("out").value(this.firstDayOut);
                json.name("in").value(this.firstDayBack);
                json.name("subject").value(this.subject);
                json.name("body").value(this.body);
            }
            json.endObject();
            json.flush();
            json.close();
        } catch (final IOException e) {
            Utils.logError(null, e);
        }

        return out.toString();
    }
}
