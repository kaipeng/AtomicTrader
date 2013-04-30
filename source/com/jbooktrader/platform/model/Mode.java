package com.jbooktrader.platform.model;

public enum Mode {
    Trade("Trading"),
    BackTest("Back Testing"),
    ForwardTest("Forward Testing"),
    Optimization("Optimizing"),
    Idle("Idle"),
    ClosingPositions("ClosingPositions");



    private final String name;

    Mode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
