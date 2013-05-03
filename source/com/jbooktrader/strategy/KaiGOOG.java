package com.jbooktrader.strategy;

import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;


/**
 *
 */
public class KaiGOOG extends StrategyStock {
    private static final String TICKER = "GOOG";

	
    // Technical indicators
    private Indicator balanceVelocityInd, priceVelocityInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String SCALE = "Scale";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit, scale;
    

    public KaiGOOG(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams, TICKER);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        scale = getParam(SCALE);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 2200, 3600, 5, 3200);
        addParam(SCALE, 5, 25, 1, 16);
        addParam(ENTRY, 20, 120, 1, 5);
        addParam(EXIT, -50, 0, 1, -4);
    }

    @Override
    public void setIndicators() {
        balanceVelocityInd = addIndicator(new BalanceVelocity(1, getParam(PERIOD)));
        priceVelocityInd = addIndicator(new PriceVelocity(1, getParam(PERIOD)));
        
    }

    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double priceVelocity = priceVelocityInd.getValue();
        
        double force = balanceVelocity - scale * priceVelocity;
        System.out.println("[Kai"+TICKER+"] Force: " + force + "\tEntry: " + entry + "\tExit: " + exit);

        if (force >= entry) {
            System.out.println("[BUY]");

            setPosition(100);
        } else if (force <= exit) {
            System.out.println("[SELL]");

            setPosition(0);
        }
    }
}