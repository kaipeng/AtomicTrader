package com.jbooktrader.platform.report;

import com.jbooktrader.platform.startup.*;

import java.io.*;


public abstract class Report {
	final boolean CONSOLEPRINT_REPORT = true;

    protected static final String FIELD_START = "<td>";
    protected static final String FIELD_END = "</td>";
    protected static final String HEADER_START = "<th>";
    protected static final String HEADER_END = "</th>";
    protected static final String ROW_START = "<tr>";
    protected static final String ROW_END = "</tr>";
    protected static final String FIELD_BREAK = "<br>";

    private static final String REPORT_DIR;
    private final PrintWriter writer;

    static {
        String fileSeparator = System.getProperty("file.separator");
        REPORT_DIR = AtomicTrader.getAppPath() + fileSeparator + "reports" + fileSeparator;
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
    }

    protected Report(String reportName) throws IOException {
        String fullFileName = REPORT_DIR + reportName + ".htm";
        StringBuilder response = new StringBuilder();

        File file = new File(fullFileName);
        if(!file.exists() || file.length() == 0){
            response.append("<html><head><title>");
            response.append(AtomicTrader.APP_NAME + ", version " + AtomicTrader.VERSION + "</title>");
            response.append("<link rel=stylesheet type=text/css href=bootstrap.min.css />");
            response.append("<link rel=stylesheet type=text/css href=JBookTrader.css />");
            response.append("<link rel=\"shortcut icon\" type=image/x-icon href=JBookTrader.ico />");
            response.append("</head><body><h2><img src=\"atomic.png\" height=40 width=55>" + AtomicTrader.APP_NAME + " - ");
            response.append("<a href=EventReport.htm target=_new>" + reportName + "</a></h2><table>");
        }
        writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));

        response.append("</table><br>"); // close the previously created table, if any
        response.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" width=100%>");
        write(response);
    }


    protected synchronized void write(StringBuilder sb) {
        if(CONSOLEPRINT_REPORT)
        	System.out.println(sb);
        
        writer.println(sb);
        writer.flush();
    }
}
