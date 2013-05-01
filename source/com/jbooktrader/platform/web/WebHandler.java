package com.jbooktrader.platform.web;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WebHandler implements HttpHandler {
    private static final DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
    private static final DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);
    private static final String RESOURCE_DIR = AtomicTrader.getAppPath() + "/resources";
    private static final String TEMPLATE_DIR = AtomicTrader.getAppPath() + "/resources/templates";
    private static final String REPORT_DIR = AtomicTrader.getAppPath() + "/reports";

    private void addRow(StringBuilder response, List<Object> cells, int rowCount) {
        response.append((rowCount % 2 == 0) ? "<tr>" : "<tr class=oddRow>");
        for (Object cell : cells) {
            response.append("<td>").append(cell).append("</td>");
        }
        response.append("</tr>");
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        byte[] out;
        String resource = httpExchange.getRequestURI().getPath();

        if (resource.equals("") || resource.equals("/")) {
            out = mainPageResponse().getBytes();
        } else {
            String path = (resource.endsWith("htm") ? REPORT_DIR : RESOURCE_DIR) + resource;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            out = new byte[(int) new File(path).length()];
            bis.read(out);
            bis.close();
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, out.length);
        OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(out);
        responseBody.close();
    }
    
    public static StringBuilder insertVariable(StringBuilder response, String key, String value){
    	int index = 0;
    	while(true){
    		index = response.indexOf("{"+key+"}");
    		if(index == -1)
    			return response;

        	response = response.replace(index, index+key.length()+2, value);
    	}
    }
    
    public static StringBuilder openTemplate(String path, Map<String, String>variables){
        StringBuilder response = new StringBuilder();
        
        try{
      	  // Open the file that is the first 
      	  // command line parameter
      	  FileInputStream fstream = new FileInputStream(path);
      	  // Get the object of DataInputStream
      	  DataInputStream in = new DataInputStream(fstream);
      	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
      	  String strLine;
      	  //Read File Line By Line
      	  while ((strLine = br.readLine()) != null) {
      		  // Print the content on the console
      		  response.append(strLine);
      	  }
      	  //Close the input stream
      	  in.close();
	    }catch (Exception e){//Catch exception if any
      	  System.out.println("Error: " + e.getMessage());
	    }

        for(String key : variables.keySet()){
        	response = insertVariable(response, key, variables.get(key));
        }

        return response;
    }

    private String mainPageResponse(){
    	Dispatcher dispatcher = Dispatcher.getInstance();
        List<Strategy> strategies = new ArrayList<Strategy>(dispatcher.getTrader().getAssistant().getAllStrategies());
        Collections.sort(strategies);
        
        HashMap<String, String> variables = new HashMap<String, String>();
        variables.put("APP_NAME", (AtomicTrader.APP_NAME));
        variables.put("VERSION", (AtomicTrader.VERSION));
        variables.put("MODE", dispatcher.getMode().getName());
        
        StringBuilder response = openTemplate(TEMPLATE_DIR + "/main.html", variables);

        int strategyRowCount = 0;
        System.out.println("Strats: " + strategies.toString());

        for (Strategy strategy : strategies) {
            System.out.println("Strat: " + strategy.getName());

            MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
            PerformanceManager pm = strategy.getPerformanceManager();

            List<Object> cells = new ArrayList<Object>();
            String path = REPORT_DIR + "/" + strategy.getName() + ".htm";
            System.out.println("Strat path: " + path);

            if (new File(path).exists()) {
                cells.add("<a href=" + strategy.getName() + ".htm target=_new>" + strategy.getName() + "</a>");
            } else {
                cells.add(strategy.getName());
            }
            cells.add(strategy.getSymbol());
            cells.add((marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "n/a");
            cells.add(strategy.getPositionManager().getCurrentPosition());
            cells.add(pm.getTrades());
            cells.add(df0.format(pm.getNetProfit()));
            System.out.println("response: " + response);

            addRow(response, cells, strategyRowCount++);
            System.out.println("Add row: " + response);

        }

        response.append("</table></body></html>");
        return response.toString();
    }
}
