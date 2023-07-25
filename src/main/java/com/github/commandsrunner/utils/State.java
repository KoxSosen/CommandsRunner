package com.github.commandsrunner.utils;

/**
 * Represents the state of the backend server.
 * STARTING - The server is starting, we wait for the bungeecord message.
 * RUNNING - The server is running, we don't have to do anything.
 * SHUTTINGDOWN - The server is shutting down.
 * STOPPED - The server is stopped.
 */
public enum State {
    STARTING,
    RUNNING,
    SHUTTINGDOWN,
    STOPPED
}
