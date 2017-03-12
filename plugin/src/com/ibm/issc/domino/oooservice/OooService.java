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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

import com.google.common.base.Charsets;
import com.google.gson.stream.JsonWriter;

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

    /**
     * Return the OOO Status for a given user. Since values are (potentially) cached
     * allow for a reset parameter force = true
     *
     * @param user
     *            a given user as eMail or Notes Name
     * @param force
     *            true will ignore a cached value
     * @return JSON structure with OOO status message - or error
     */
    @GET
    public Response getOOOStatus(@PathParam("user") final String user, @QueryParam("force") final boolean force) {
        Response response = null;
        try {
            response = this.retrieveOOO(user, force);
        } catch (final Exception e) {
            Utils.logError(this.logger, e);
            response = this.getErrorResponse(e);
        }
        return response;
    }

    /**
     * Core function that returns the OOO Status
     *
     * @param user
     *            The user (email/notesname) to query
     * @param force
     *            - true: ignore cache
     * @return JSON structure
     */
    private Response retrieveOOO(final String username, final boolean force) {

        Database db = null;
        Session session = null;
        Registration registration = null;
        Document doc = null;
        Response response = null;
        final ResponseBuilder rb = Response.ok();

        NotesThread.sinitThread();
        try {
            session = NotesFactory.createSession();
            registration = session.createRegistration();
            // A server plugin has the server as userName in its session
            // TODO: needs inclusion?
            // registration.setRegistrationServer(session.getUserName());

            final StringBuffer mailserver = new StringBuffer();
            final StringBuffer mailfile = new StringBuffer();
            final StringBuffer maildomain = new StringBuffer();
            final StringBuffer mailsystem = new StringBuffer();
            @SuppressWarnings("rawtypes")
            final Vector profile = new Vector();
            registration.getUserInfo(username, mailserver, mailfile, maildomain, mailsystem, profile);

            db = session.getDatabase(mailserver.toString(), mailfile.toString());
            // TODO: Fix error handling here!
            if (!db.isOpen()) {
                db.open();
            }

            final boolean OOenabled = db.getOption(Database.DBOPT_OUTOFOFFICEENABLED);
            final OooStatus ooStatus = new OooStatus(username, OOenabled);

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

            response = rb.build();

        } catch (final NotesException e) {
            Utils.logError(this.logger, e);
            response = this.getErrorResponse(e);
        }

        Utils.shred(doc, db, registration, session);

        NotesThread.stermThread();

        return response;
    }

    /**
     * @param doc
     * @param ooStatus
     */
    @SuppressWarnings("rawtypes")
    private void retrieveOOOParameters(final Document doc, final OooStatus ooStatus) {

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
        }
    }

    /**
     * Create a full error message as JSON object into a response builder
     *
     * @param rb
     *            the response builder
     * @param e
     *            the error message
     */
    protected void errorResult(final ResponseBuilder rb, final Exception e) {

        final OutputStream out = new ByteArrayOutputStream();

        // Getting the right error message is a little tricky
        // the exception could be null or a NotesException that has .text instead of getMessage()
        // or some other exception
        final String message = (e == null) ? "No exception provided" : ((e instanceof NotesException) ? ((NotesException) e).text
                : ((e.getMessage() == null) ? e.getClass().getName() : e.getMessage()));

        final int status = (e == null) ? 500 : 503;

        try {
            final JsonWriter json = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8)));
            json.beginObject();
            json.name("error");
            json.beginObject();
            json.name("failure").value("Something went wrong");
            json.name("message").value(message);
            json.endObject();
            json.name("trace");
            json.beginArray();
            if (e != null) {
                for (final StackTraceElement ste : e.getStackTrace()) {
                    json.value(ste.toString());
                }
            }
            json.endArray();
            json.endObject();
            json.flush();
            json.close();
        } catch (final IOException ex) {
            Utils.logError(this.logger, ex);
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
