/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice OooService.java
 */
package com.ibm.issc.domino.oooservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Registration;
import lotus.domino.Session;

import org.apache.wink.common.annotations.Workspace;

/**
 * Allows to query the OOOService of a provided
 * eMail
 *
 * @author stw
 */
@Workspace(workspaceTitle = "OOO Query", collectionTitle = "Retrieves the common Out Of Office Status")
@Path(value = "{user}")
@Produces(MediaType.APPLICATION_JSON)
public class OooService {
    /* Created: 11 Mar, 2017 */

    private final Logger logger = Activator.getLogger(this.getClass().getName());

    @GET
    public Response getOOOStatus(@PathParam("user") String user) {
        Response response = null;
        try {
            response = this.retrieveOOO(user);
        } catch (final Exception e) {
            Utils.logError(this.logger, e);
            response = this.getErrorResponse(e);
        }
        return response;
    }

    /**
     * @param user
     * @return
     */
    private Response retrieveOOO(final String username) {

        Database db = null;
        Session session = null;
        Registration registration = null;
        Document doc = null;

        final ResponseBuilder rb = Response.ok();

        NotesThread.sinitThread();
        try {
            session = NotesFactory.createSession();
            registration = session.createRegistration();
            // A server plugin has the server as userName in its session
            // TODO: needs inclusion?
            // registration.setRegistrationServer(session.getUserName());

            StringBuffer mailserver = new StringBuffer();
            StringBuffer mailfile = new StringBuffer();
            StringBuffer maildomain = new StringBuffer();
            StringBuffer mailsystem = new StringBuffer();
            @SuppressWarnings("rawtypes")
            Vector profile = new Vector();
            registration.getUserInfo(username, mailserver, mailfile, maildomain, mailsystem, profile);

            db = session.getDatabase(mailserver.toString(), mailfile.toString());
            // TODO: Fix error handling here!
            if (!db.isOpen()) {
                db.open();
            }

            boolean OOenabled = db.getOption(Database.DBOPT_OUTOFOFFICEENABLED);
            OooStatus ooStatus = new OooStatus(username, OOenabled);

            // Retrieve the message only if it is active
            if (OOenabled) {
                // TODO: Better error handling
                doc = db.getProfileDocument("outofofficeprofile", null);
                if (doc != null) {
                    this.retrieveOOOParameters(doc, ooStatus);
                }
            }

            rb.status(200);
            rb.entity(ooStatus.toString()).type(MediaType.APPLICATION_JSON + "; charset=utf-8");

        } catch (NotesException e) {
            // TODO Add proper error feedback to client
            Utils.logError(this.logger, e);
        }

        Utils.shred(doc, db, registration, session);

        NotesThread.stermThread();

        return rb.build();
    }

    /**
     * @param doc
     * @param ooStatus
     */
    @SuppressWarnings("rawtypes")
    private void retrieveOOOParameters(Document doc, OooStatus ooStatus) {

        try {
            doc.setPreferJavaDates(true);
            Vector firstDayOutVector = doc.getItemValue("dateFirstDayOut");
            if ((firstDayOutVector != null) && !firstDayOutVector.isEmpty()) {
                Date fdoTime = (Date) firstDayOutVector.get(0);
                ooStatus.setFirstDayOut(fdoTime);
            }
            Vector firstDayBackVector = doc.getItemValue("dateFirstDayBack");
            if ((firstDayBackVector != null) && !firstDayBackVector.isEmpty()) {
                Date fdb = (Date) firstDayBackVector.get(0);
                ooStatus.setFirstDayBack(fdb);
            }
            ooStatus.setSubject(doc.getItemValueString("daysoutdisplay"));
            ooStatus.setBody(doc.getItemValueString("generalmessage"));

        } catch (Exception e) {
            Utils.logError(this.logger, e);
        }
    }

    /**
     * Removes new lines, carriage returns, angle brackets
     *
     * @param message
     *            the incoming message
     * @return a sanitized message
     */
    private String sanitize(String message) {

        return String.valueOf(message).replace("<", "&lt;").replace(">", "&gt;").replace("\n", "").replace("\r", "");

    }

    /**
     * @param rb
     * @param e
     */
    protected void errorResult(final ResponseBuilder rb, final Exception e) {

        final OutputStream out = new ByteArrayOutputStream();
        final Writer w = new PrintWriter(out);
        final String message = (e == null) ? "No exception provided" : ((e.getMessage() == null) ? e.getClass().getName() : e
                .getMessage());

        final int status = (e == null) ? 500 : 503;

        try {
            w.write("{\"error\" : {\n");
            w.write("\"failure\" : \"Something went wrong\",\n\"message\": \"");
            w.write(this.sanitize(message));
            w.write("\"\n},\n");
            w.write("\"trace\" : [\n");
            if (e != null) {
                for (final StackTraceElement ste : e.getStackTrace()) {
                    w.write("\"");
                    w.write(ste.toString());
                    w.write("\",\n");
                }
            }
            w.write("\"EndofTrace\"\n]\n}");
            w.close();
        } catch (final IOException ex) {
            // TODO: FIX Error reporting to client
            Utils.logError(this.logger, e);
        }

        rb.status(status).type(MediaType.APPLICATION_JSON).entity(out.toString());

    }

    /**
     * Provides an Error response back to the browser
     *
     * @param e
     *            Exception thrown
     * @return Response with status and explanation
     */
    protected Response getErrorResponse(final Exception e) {
        final ResponseBuilder rb = Response.serverError();
        this.errorResult(rb, e);
        return rb.build();
    }

}
