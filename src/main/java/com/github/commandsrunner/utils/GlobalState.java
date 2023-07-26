package com.github.commandsrunner.utils;

public class GlobalState {

    // Default state is STOPPED, as we aren't running.
    static State state = State.STOPPED;

    public static void setState(State state1) {
        state = state1;
    }

    public static State getState() {
        return state;
    }

}
