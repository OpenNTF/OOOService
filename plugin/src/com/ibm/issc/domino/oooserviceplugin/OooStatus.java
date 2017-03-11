/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooserviceplugin OooStatus.java
 */
package com.ibm.issc.domino.oooserviceplugin;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author stw
 */
public class OooStatus {
    // JSON compatible date format
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
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @param firstDayBack
     *            the firstDayBack to set
     */
    public void setFirstDayBack(Date firstDateBack) {
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
    public void setFirstDayOut(Date firstDate) {
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
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append("\"user\":\"");
        b.append(this.user);
        b.append("\", ");
        b.append("\"enabled\": ");
        b.append(String.valueOf(this.enabled));
        if (this.enabled) {
            this.append(b, "out", this.firstDayOut);
            this.append(b, "in", this.firstDayBack);
            this.append(b, "subject", this.subject);
            this.append(b, "body", this.body);
        }
        b.append("}");
        return b.toString();
    }

    /**
     * @param b
     *            StringBuilder
     * @param label
     *            the Label to use
     * @param value
     *            the Value it has
     */
    private void append(final StringBuilder b, final String label, final String value) {
        // TODO: needs a JSON save way of rendering this!!
        if (value != null) {
            b.append(", ");
            b.append("\"");
            b.append(label);
            b.append("\": \"");
            b.append(value);
            b.append("\"");
        }
    }
}
