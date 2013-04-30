package com.jbooktrader.platform.dialog;


import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.StrategyTableColumn.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.text.*;

/**
 * Main application window. All the system logic is intentionally left out if this class,
 * which acts as a simple "view" of the underlying model.
 */
public class MainFrameDialog extends JFrame implements ModelListener {
    private final Toolkit toolkit;
    private JLabel timeLabel;
    private JMenuItem stopAllMenuItem, tradeAllMenuItem, resetMenuItem, backTestAllMenuItem,forwardTestAllMenuItem, closeAllPositionsMenuItem;
    private JMenuItem exitMenuItem, aboutMenuItem, userManualMenuItem, discussionMenuItem, releaseNotesMenuItem, projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, backTestMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem, closePositionMenuItem, stopMenuItem;
    private StrategyTableModel strategyTableModel;
    private JTable strategyTable;
    private JPopupMenu popupMenu;
    private SimpleDateFormat sdf;

    public MainFrameDialog() throws JBookTraderException {
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        setVisible(true);
    }

    public void modelChanged(Event event, Object value) {
        switch (event) {
            case ModeChanged:
                Mode mode = Dispatcher.getInstance().getMode();
                if (mode == Mode.Trade || mode == Mode.ForwardTest || mode == Mode.ClosingPositions) {
                    backTestMenuItem.setEnabled(false);
                    optimizeMenuItem.setEnabled(false);
                    chartMenuItem.setEnabled(false);
                    if (mode == Mode.Trade || mode == Mode.ClosingPositions) {
                        forwardTestMenuItem.setEnabled(false);
                    }
                    if (mode == Mode.ForwardTest) {
                        tradeMenuItem.setEnabled(false);
                    }
                }
                if (mode == Mode.BackTest || mode == Mode.Optimization) {
                    forwardTestMenuItem.setEnabled(false);
                    tradeMenuItem.setEnabled(false);
                }
                if(mode == Mode.Idle){
                    backTestMenuItem.setEnabled(true);
                    optimizeMenuItem.setEnabled(true);
                    chartMenuItem.setEnabled(true);
                    forwardTestMenuItem.setEnabled(true);
                    tradeMenuItem.setEnabled(true);
                    closePositionMenuItem.setEnabled(true);
                }else{
                    stopMenuItem.setEnabled(true);
                }
                setTitle(AtomicTrader.APP_NAME + " - [" + mode.getName() + "]");
                break;
            case Error:
                String msg = (String) value;
                MessageDialog.showError(msg);
                break;
            case TimeUpdate:
                timeLabel.setText(sdf.format(value));
                break;
            case StrategyUpdate:
                Strategy strategy = (Strategy) value;
                strategyTableModel.update(strategy);
                break;
        }
    }

    public void userManualAction(ActionListener action) {
        userManualMenuItem.addActionListener(action);
    }

    public void discussionAction(ActionListener action) {
        discussionMenuItem.addActionListener(action);
    }

    public void releaseNotesAction(ActionListener action) {
        releaseNotesMenuItem.addActionListener(action);
    }

    public void projectHomeAction(ActionListener action) {
        projectHomeMenuItem.addActionListener(action);
    }

    public void strategyTableAction(MouseAdapter action) {
        strategyTable.addMouseListener(action);
    }

    public void informationAction(ActionListener action) {
        infoMenuItem.addActionListener(action);
    }

    public void backTestAction(ActionListener action) {
        backTestMenuItem.addActionListener(action);
    }

    public void optimizeAction(ActionListener action) {
        optimizeMenuItem.addActionListener(action);
    }

    public void forwardTestAction(ActionListener action) {
        forwardTestMenuItem.addActionListener(action);
    }

    public void tradeAction(ActionListener action) {
        tradeMenuItem.addActionListener(action);
    }

    public void chartAction(ActionListener action) {
        chartMenuItem.addActionListener(action);
    }
    public void stopAction(ActionListener action) {
    	stopMenuItem.addActionListener(action);
    }
    public void closeAllPositionsAction(ActionListener action) {
    	closeAllPositionsMenuItem.addActionListener(action);
    }
    public void closePositionAction(ActionListener action) {
    	closePositionMenuItem.addActionListener(action);
    }


    public void preferencesAction(ActionListener action) {
        preferencesMenuItem.addActionListener(action);
    }

    public void exitAction(ActionListener action) {
        exitMenuItem.addActionListener(action);
    }

    public void exitAction(WindowAdapter action) {
        addWindowListener(action);
    }

    public void aboutAction(ActionListener action) {
        aboutMenuItem.addActionListener(action);
    }
    
    public void tradeAllAction(ActionListener action) {
    	tradeAllMenuItem.addActionListener(action);
    }
    public void stopAllAction(ActionListener action) {
    	stopAllMenuItem.addActionListener(action);
    }
    public void backTestAllAction(ActionListener action) {
    	backTestAllMenuItem.addActionListener(action);
    }
    public void forwardTestAllAction(ActionListener action) {
    	forwardTestAllMenuItem.addActionListener(action);
    }
    public void resetAction(ActionListener action) {
    	resetMenuItem.addActionListener(action);
    }
    

    private URL getImageURL(String imageFileName) throws JBookTraderException {
        URL imgURL = ClassLoader.getSystemResource(imageFileName);
        if (imgURL == null) {
            String msg = "Could not locate " + imageFileName + ". Make sure the /resources directory is in the classpath.";
            throw new JBookTraderException(msg);
        }
        return imgURL;
    }

    private ImageIcon getImageIcon(String imageFileName) throws JBookTraderException {
        return new ImageIcon(toolkit.getImage(getImageURL(imageFileName)));
    }


    private void populateStrategies() {
        for (Strategy strategy : ClassFinder.getStrategies()) {
            strategyTableModel.addStrategy(strategy);
        }
    }

    public StrategyTableModel getStrategyTableModel() {
        return strategyTableModel;
    }

    public JTable getStrategyTable() {
        return strategyTable;
    }

    public void showPopup(MouseEvent mouseEvent) {
        popupMenu.show(strategyTable, mouseEvent.getX(), mouseEvent.getY());
    }

    private void init() throws JBookTraderException {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        sdf = new SimpleDateFormat("HH:mm:ss");

        // session menu
        JMenu sessionMenu = new JMenu("Session");
        sessionMenu.setMnemonic('S');
        
        forwardTestAllMenuItem = new JMenuItem("Foward Test ALL", getImageIcon("forwardTest.png"));
        forwardTestAllMenuItem.setMnemonic('F');
        sessionMenu.add(forwardTestAllMenuItem);

        backTestAllMenuItem = new JMenuItem("Back Test ALL", getImageIcon("backTest.png"));
        backTestAllMenuItem.setMnemonic('B');
        sessionMenu.add(backTestAllMenuItem);

        sessionMenu.addSeparator();

        tradeAllMenuItem = new JMenuItem("TRADE ALL", getImageIcon("trade.png"));
        tradeAllMenuItem.setMnemonic('T');
        sessionMenu.add(tradeAllMenuItem);

        stopAllMenuItem = new JMenuItem("STOP ALL\nORDERS", getImageIcon("stop.png"));
        stopAllMenuItem.setMnemonic('S');
        sessionMenu.add(stopAllMenuItem);
        
        closeAllPositionsMenuItem = new JMenuItem("CLOSE ALL\nPOSITIONS", getImageIcon("close.png"));
        closeAllPositionsMenuItem.setMnemonic('C');
        sessionMenu.add(closeAllPositionsMenuItem);

        sessionMenu.addSeparator();

        resetMenuItem = new JMenuItem("Reset");
        resetMenuItem.setMnemonic('R');  
        sessionMenu.add(resetMenuItem);

        sessionMenu.addSeparator();
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic('X');
        sessionMenu.add(exitMenuItem);

        // configure menu
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('C');
        preferencesMenuItem = new JMenuItem("Preferences...");
        preferencesMenuItem.setMnemonic('P');
        configureMenu.add(preferencesMenuItem);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        userManualMenuItem = new JMenuItem("User Manual");
        userManualMenuItem.setMnemonic('U');
        releaseNotesMenuItem = new JMenuItem("Release Notes");
        releaseNotesMenuItem.setMnemonic('R');
        discussionMenuItem = new JMenuItem("Discussion Group");
        discussionMenuItem.setMnemonic('D');
        projectHomeMenuItem = new JMenuItem("Project Home");
        projectHomeMenuItem.setMnemonic('P');
        aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.setMnemonic('A');
        helpMenu.add(userManualMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(releaseNotesMenuItem);
        helpMenu.add(discussionMenuItem);
        helpMenu.add(projectHomeMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutMenuItem);
        
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(sessionMenu);
        menuBar.add(configureMenu);
        menuBar.add(helpMenu);
        menuBar.add(tradeAllMenuItem);
        menuBar.add(stopAllMenuItem);
        setJMenuBar(menuBar);

        // popup menu
        popupMenu = new JPopupMenu();

        infoMenuItem = new JMenuItem("Information", getImageIcon("information.png"));
        backTestMenuItem = new JMenuItem("Back Test", getImageIcon("backTest.png"));
        optimizeMenuItem = new JMenuItem("Optimize", getImageIcon("optimize.png"));
        forwardTestMenuItem = new JMenuItem("Forward Test", getImageIcon("forwardTest.png"));
        chartMenuItem = new JMenuItem("Chart", getImageIcon("chart.png"));
        tradeMenuItem = new JMenuItem("Trade");
        closePositionMenuItem = new JMenuItem("Close Position");
        stopMenuItem = new JMenuItem("Stop");


        popupMenu.add(infoMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(optimizeMenuItem);
        popupMenu.add(backTestMenuItem);
        popupMenu.add(forwardTestMenuItem);
        popupMenu.add(chartMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(tradeMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(closePositionMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(stopMenuItem);



        JScrollPane strategyTableScrollPane = new JScrollPane();
        strategyTableScrollPane.setAutoscrolls(true);
        strategyTableModel = new StrategyTableModel();
        strategyTable = new JTable(strategyTableModel);
        strategyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        strategyTable.setShowGrid(false);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) strategyTable.getDefaultRenderer(String.class);
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);

        // Make some columns wider than the rest, so that the info fits in.
        TableColumnModel columnModel = strategyTable.getColumnModel();
        columnModel.getColumn(Strategy.ordinal()).setPreferredWidth(175);

        strategyTableScrollPane.getViewport().add(strategyTable);

        Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JBookTrader.png"));
        setIconImage(appIcon);

        add(strategyTableScrollPane, BorderLayout.CENTER);

        JToolBar statusBar = new JToolBar();
        statusBar.setLayout(new BorderLayout());
        statusBar.setFloatable(false);
        timeLabel = new JLabel(" ");

        statusBar.add(timeLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        setTitle(AtomicTrader.APP_NAME);

        setMinimumSize(new Dimension(600, 200));
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        int width = prefs.getInt(MainWindowWidth);
        int height = prefs.getInt(MainWindowHeight);
        pack();
        setSize(width, height);
        setLocationRelativeTo(null);
    }
}
