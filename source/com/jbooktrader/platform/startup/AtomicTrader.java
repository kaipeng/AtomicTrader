package com.jbooktrader.platform.startup;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.io.*;
import java.nio.channels.*;


/**
 * Application starter.
 */
public class AtomicTrader {
    public static final boolean TEST_MODE = true;

    public static final String APP_NAME = "Atomic Trader";
    public static final String VERSION = ".1";
    public static final String RELEASE_DATE = "4/22/2013";

    private static String appPath;
    private static String runMode;


    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private AtomicTrader(String runMode) throws JBookTraderException {
        try {
            Dispatcher.getInstance().setReporter();
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }
        new MainFrameController(runMode);
    }

    /**
     * Starts JBookTrader application.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            File file = new File(System.getProperty("user.home"), APP_NAME + ".tmp");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            if (channel.tryLock() == null) {
                MessageDialog.showMessage(APP_NAME + " is already running.");
                return;
            }

            if (args.length < 1) {
                String msg = "At least one argument must be passed. Usage: JBookTrader <JBookTraderDirectory> <RunMode ''/'trade'/'forwardtest'/'closingpositions'>";
                throw new JBookTraderException(msg);
            }
            appPath = args[0];
            runMode = args[1];
            new AtomicTrader(runMode);
        } catch (Throwable t) {
            MessageDialog.showException(t);
        }
    }

    public static String getAppPath() {
        return appPath;
    }

}
