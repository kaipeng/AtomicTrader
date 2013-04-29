package com.jbooktrader.platform.util;

import com.ib.client.*;

/**
 * Provides convenience methods to create stock, futures, and Forex contracts
 */
public class ContractFactory {

    public static Contract makeContract(String symbol, String securityType, String exchange, String expiry, String currency) {
        Contract contract = new Contract();

        contract.m_symbol = symbol;
        contract.m_secType = securityType;
        contract.m_exchange = exchange;
        contract.m_expiry = expiry;
        contract.m_currency = currency;

        return contract;
    }
    
    public static Contract makeContract(String contractId, String symbol, String securityType, String exchange, String expiry, String strike, String putOrCall, String currency,
    		String multiplier, String secIdType, String secId, String localSymbol, String includeExpired) {
        Contract m_contract = new Contract();

        // set contract fields
        m_contract.m_conId = ParseInt(contractId, 0);
        m_contract.m_symbol = symbol;
        m_contract.m_secType = securityType;
        m_contract.m_expiry = expiry;
       	m_contract.m_strike = ParseDouble(strike, 0.0);
        m_contract.m_right = putOrCall;
        m_contract.m_multiplier = multiplier;
        m_contract.m_exchange = exchange;
        m_contract.m_currency = currency;
        m_contract.m_localSymbol = localSymbol;
        try {
        	int m_includeExpired = Integer.parseInt(includeExpired);
        	m_contract.m_includeExpired = (m_includeExpired == 1);
        }
        catch (NumberFormatException ex) {
        	m_contract.m_includeExpired = false;
        }
        m_contract.m_secIdType = secIdType;
        m_contract.m_secId = secId;
        
        return m_contract;
    }
    
    private static int ParseInt(String text, int defValue) {
    	try {
    		return Integer.parseInt(text);
    	}
    	catch (NumberFormatException e) {
    		return defValue;
    	}
    }
    private static double ParseDouble(String text, double defValue) {
    	try {
    		return Double.parseDouble(text);
    	}
    	catch (NumberFormatException e) {
    		return defValue;
    	}
    }
    

    public static Contract makeStockContract(String symbol, String exchange, String currency) {
        return makeContract(symbol, "STK", exchange, null, currency);
    }

    public static Contract makeStockContract(String symbol, String exchange) {
        return makeStockContract(symbol, exchange, null);
    }

    public static Contract makeFutureContract(String symbol, String exchange, String expiry, String currency) {
        return makeContract(symbol, "FUT", exchange, expiry, currency);
    }

    public static Contract makeFutureContract(String symbol, String exchange, String expiry) {
        return makeFutureContract(symbol, exchange, expiry, null);
    }

    public static Contract makeFutureContract(String symbol, String exchange) {
        return makeFutureContract(symbol, exchange, MostLiquidContract.getMostLiquid());
    }

    public static Contract makeCashContract(String symbol, String currency) {
        return makeContract(symbol, "CASH", "IDEALPRO", null, currency);
    }

    public static Contract makeIndexContract(String symbol, String exchange) {
        return makeContract(symbol, "IND", exchange, null, null);
    }

}
