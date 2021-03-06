package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * This class acts as a "wrapper" in the IB's API terminology.
 */
public class Trader extends EWrapperAdapter {
	final boolean CONSOLEPRINT_IBDATAIN = true;

    private final EventReport eventReport;
    private final TraderAssistant traderAssistant;
    private String previousErrorMessage;

    public Trader() {
        traderAssistant = new TraderAssistant(this);
        previousErrorMessage = "";
        eventReport = Dispatcher.getInstance().getEventReport();
    }

    public TraderAssistant getAssistant() {
        return traderAssistant;
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try {
            if (key.equalsIgnoreCase("AccountCode")) {
                synchronized (this) {
                    traderAssistant.setAccountCode(value);
                    notifyAll();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        String newsBulletin = "Msg ID: " + msgId + " Msg Type: " + msgType + " Msg: " + message + " Exchange: " + origExchange;
        eventReport.report("IB API", newsBulletin);
    }


    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        try {
            int orderId = execution.m_orderId;
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();
            OpenOrder openOrder = openOrders.get(orderId);
            if (openOrder != null) {
                openOrder.add(execution);
                if (openOrder.isFilled()) {
                    Strategy strategy = openOrder.getStrategy();
                    PositionManager positionManager = strategy.getPositionManager();
                    positionManager.update(openOrder);
                    openOrders.remove(orderId);
                    traderAssistant.resetOrderExecutionPending();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void execDetailsEnd(int reqId) {
        try {
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();
            String msg = "Execution for this order was not found.";
            msg += " In all likelihood, this is because the order was placed while TWS was disconnected from the server.";
            msg += " This order will be removed and another one will be submitted. The strategy will continue to run normally.";

            for (OpenOrder openOrder : openOrders.values()) {
                String orderMsg = "Order " + openOrder.getId() + ": " + msg;
                eventReport.report(openOrder.getStrategy().getName(), orderMsg);
            }

            openOrders.clear();
            traderAssistant.resetOrderExecutionPending();

        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }
    
    @Override
    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
    	//TODO:implement change in order status
    	//IMplement partial fill
    	
    	
    	//cancelled order
    	if(status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Inactive") || status.equalsIgnoreCase("Filled")){
    		try{
    			traderAssistant.getOpenOrders().remove(orderId);
    		} catch (Throwable t) {
                // Do not allow exceptions come back to the socket -- it will cause disconnects
                eventReport.report(t);
            }
    		finally{
    			if(traderAssistant.getOpenOrders().size() == 0){
    		    	traderAssistant.isCancelingAllOpenOrders = false;
    			}
    		}
    	}
    }
    
    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState ){
    	//Read cancel queue
    	if(traderAssistant.isCancelingAllOpenOrders){
    		traderAssistant.cancelOrder(orderId);
    	}
	}

    @Override
    public void contractDetails(int id, ContractDetails cd) {
        String lineSep = "<br>";
        for(Strategy strat : traderAssistant.getAllStrategies()){
        	if(strat.getContract().m_conId == cd.m_underConId){
        		strat.setContractDetails(cd);
        	}
        }
        StringBuilder details = new StringBuilder("Contract details:").append(lineSep);
        details.append("Trading class: ").append(cd.m_tradingClass).append(lineSep);
        details.append("Exchanges: ").append(cd.m_validExchanges).append(lineSep);
        details.append("Long name: ").append(cd.m_longName).append(lineSep);
        details.append("Market name: ").append(cd.m_marketName).append(lineSep);
        details.append("Minimum tick: ").append(cd.m_minTick).append(lineSep);
        details.append("Contract month: ").append(cd.m_contractMonth).append(lineSep);
        details.append("Time zone id: ").append(cd.m_timeZoneId).append(lineSep);
        details.append("Trading hours: ").append(cd.m_tradingHours).append(lineSep);
        details.append("Liquid hours: ").append(cd.m_liquidHours).append(lineSep);
        eventReport.report("IB API", details.toString());
    }

    @Override
    public void error(Exception e) {
        eventReport.report("IB API", e.toString());
    }

    @Override
    public void error(String error) {
        eventReport.report("IB API", error);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        try {
            String msg = errorCode + ": " + errorMsg;
            if (id != -1) {
                msg += " (for id " + id + ")";
            }

            if (msg.equals(previousErrorMessage)) {
                // ignore duplicate error messages
                return;
            }

            previousErrorMessage = msg;
            eventReport.report("IB API", msg);

            // Errors 1101 and 1102 are sent when connectivity is restored.
            boolean isConnectivityRestored = (errorCode == 1101 || errorCode == 1102);
            if (isConnectivityRestored) {
                if (!traderAssistant.getOpenOrders().isEmpty()) {
                    eventReport.report(AtomicTrader.APP_NAME, "Checking for executions while TWS was disconnected from the IB server.");
                    traderAssistant.requestExecutions();
                }
            }

            if (errorCode == 317) {// Market depth data has been reset
                traderAssistant.getMarketBook(id).getMarketDepth().reset();
                eventReport.report(AtomicTrader.APP_NAME, "Market data for book " + id + " has been reset.");
            }

            if (errorCode == 202) { // Order Canceled - reason:Can't handle negative priced order
                traderAssistant.getOpenOrders().remove(id);
                traderAssistant.resetOrderExecutionPending();
                String reportMsg = "Removed order " + id + " because IB reported error " + errorCode + ". ";
                reportMsg += "Another order will be submitted. The strategy will continue to run normally";
                eventReport.report(AtomicTrader.APP_NAME, reportMsg);
            }

            // 200: bad contract
            // 309: market depth requested for more than 3 symbols
            boolean isInvalidRequest = (errorCode == 200 || errorCode == 309);
            if (isInvalidRequest) {
                Dispatcher.getInstance().fireModelChanged(Event.Error, "IB reported: " + errorMsg);
            }


        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }


    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        try {
            MarketDepth marketDepth = traderAssistant.getMarketBook(tickerId).getMarketDepth();
            marketDepth.update(position, MarketDepthOperation.getOperation(operation), MarketDepthSide.getSide(side), price, size);
            if(CONSOLEPRINT_IBDATAIN){
            //	System.out.println("[MktDepth] TckrID: " + tickerId + "\tPos: " + position + "\tOp: " + operation + "\tSide: " + side + "\tPx: " + price + "\tSize: " + size);
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void tickSize(int tickerId, int tickType, int size) {
        try {
            if (tickType == TickType.VOLUME && size != 0) {
                MarketDepth marketDepth = traderAssistant.getMarketBook(tickerId).getMarketDepth();
                marketDepth.update(size);
                if(CONSOLEPRINT_IBDATAIN)
                	System.out.println("[TckSize] TckrID: " + tickerId + "\tType: " + tickType + "\tSize: " + size);
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }
    
    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        try {
            if(CONSOLEPRINT_IBDATAIN)
               	System.out.println("[TckGen] TckrID: " + tickerId + "\tType: " + tickType + "\tVal: " + value);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void nextValidId(int orderId) {
        traderAssistant.setOrderID(orderId);
    }

}
