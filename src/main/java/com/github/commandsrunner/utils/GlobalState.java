package com.github.commandsrunner.utils;

public class GlobalState {
    static State state;

    public static void setState(State state1) {
        state = state1;
    }

    public static State getState() {
        return state;
    }
}
