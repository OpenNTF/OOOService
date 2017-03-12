/** ========================================================================= *
 * Copyright (C)  2016 IBM Corporation ( http://www.ibm.com/ )                *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <st.wissel@sg.ibm.com>                *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 * com.ibm.issc.domino.OOOServicePlugin com.ibm.issc.domino.oooservice Stats.java
 */
package com.ibm.issc.domino.oooservice;

import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.annotations.Workspace;

import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

/**
 * Returns statistics about calls to the OOOService
 *
 * @author stw
 */
@Workspace(workspaceTitle = "OOO Query", collectionTitle = "Shows statistics of ooo useage")
@Path(value = "/stats")
@Produces(MediaType.TEXT_HTML)
public class Stats {
    @GET
    public String getStats(@Context final HttpServletRequest req, @Context final HttpServletResponse resp) throws Exception {

        final StringBuilder sb = new StringBuilder();
        resp.setContentType("text/html");

        sb.append("<htm>\n<head>\n<title>OrangeBox Statistics</title>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("html, body, td {font-family: Verdana, Arial, sans-serif; font-size: small;}\n");
        sb.append("td {padding: 8px; margin: 0; border-bottom: 1px solid gray; border-left: 1px solid gray;}\n");
        sb.append("</style>\n");
        sb.append("</head>\n<body>\n<h1>Statistics</h1>\n");
        this.addReport(sb);
        sb.append("\n</body>\n</html>\n");

        return sb.toString();
    }

    /**
     * @param sb
     *            String builder to send report to
     */
    private void addReport(final StringBuilder sb) {

        final MonitorComposite rootMon = MonitorFactory.getRootMonitor();
        final com.jamonapi.Monitor[] monitors = rootMon.getMonitors();
        Arrays.sort(monitors, new Comparator<com.jamonapi.Monitor>() {
            @Override
            public int compare(final com.jamonapi.Monitor first, final com.jamonapi.Monitor second) {
                return first.getLabel().compareTo(second.getLabel());
            }
        });
        sb.append("<table>");
        sb.append("<thead><tr>");
        sb.append("<th>Label</th>");
        sb.append("<th align=\"right\">Hits</th>");
        sb.append("<th align=\"right\">Min</th>");
        sb.append("<th align=\"right\">Avg</th>");
        sb.append("<th align=\"right\">Max</th>");
        sb.append("<th align=\"right\">StdDev</th>");
        sb.append("<th align=\"right\">AvgActive</th>");
        sb.append("<th align=\"right\">MaxActive</th>");
        sb.append("</tr></thead><tbody>");
        for (final com.jamonapi.Monitor mon : monitors) {
            if (!mon.getLabel().equals("com.jamonapi.Exceptions")) {
                sb.append("<tr>");
                sb.append("<td>");
                sb.append(mon.getLabel());
                sb.append("</td>");
                sb.append("<td align=\"right\">");
                sb.append(String.format("%.1f", mon.getHits()));
                sb.append("</td>");
                sb.append("<td align=\"right\">");
                sb.append(String.format("%.1f", mon.getMin()));
                sb.append("</td>");
                sb.append("<td align=\"right\"><b>");
                sb.append(String.format("%.1f", mon.getAvg()));
                sb.append("</b></td>");
                sb.append("<td  align=\"right\" style=\"color: red;\">");
                sb.append(String.format("%.1f", mon.getMax()));
                sb.append("</td>");
                sb.append("<td align=\"right\">");
                sb.append(String.format("%.1f", mon.getStdDev()));
                sb.append("</td>");
                sb.append("<td align=\"right\">");
                sb.append(String.format("%.1f", mon.getAvgActive()));
                sb.append("</td>");
                sb.append("<td align=\"right\">");
                sb.append(String.format("%.1f", mon.getMaxActive()));
                sb.append("</td>");
                sb.append("</tr>");
            }
        }
        sb.append("</tbody></table>");

    }

}
