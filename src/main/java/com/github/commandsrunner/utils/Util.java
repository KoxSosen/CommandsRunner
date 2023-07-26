package com.github.commandsrunner.utils;

import com.github.commandsrunner.CommandsRunner;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Util {

    public void executeCommands(List<String> commands) {
        for (String command : commands) {
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception e) {
                throw new RuntimeException("Issue executing command: " + e);
            }
        }
    }

    public boolean isEnoughPlayers(ProxyServer proxyServer) {
        return proxyServer.getPlayerCount() > 0;
    }

    public void runShutdownCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("sudo systemctl stop backend");
        executeCommands(commands);
    }

    public void runStartupCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("sudo systemctl start backend");
        executeCommands(commands);
    }

    public void runServerLogic(ProxyServer proxyServer, CommandsRunner commandsRunner, Logger logger) {
        // If there are more than 0 players online, the server should be running.
        // If there is less than 1, that means that we can shut down.
        if (!isEnoughPlayers(proxyServer)) {
            // Only shutdown if we are running
            // If we are starting/shutting down, that means that someone tired to join, or we are still shutting down for some reason.
            // Also, this avoids spamming requests, as if we are stopped, we don't need to stop anything again.
            if (GlobalState.getState().equals(State.RUNNING)) {
                runShutdownCommands();
                GlobalState.setState(State.SHUTTINGDOWN);
                logger.info("Stopped backend due to player inactivity.");

                // It shouldn't take more than 20 seconds for the server to shut down
                // Meaning we can set its tate to stopped
                proxyServer.getScheduler()
                        .buildTask(commandsRunner, () -> GlobalState.setState(State.STOPPED))
                        .delay(20L, TimeUnit.SECONDS)
                        .schedule();
            }
        } else {
            GlobalState.setState(State.RUNNING);
        }
    }

    public void startServer(ProxyServer proxyServer, CommandsRunner commandsRunner, Logger logger) {
        runStartupCommands();
        GlobalState.setState(State.STARTING);

        // It shouldn't take more than 20 seconds for the server to start
        // Meaning we can set its tate to running
        proxyServer.getScheduler()
                .buildTask(commandsRunner, () -> GlobalState.setState(State.RUNNING))
                .delay(20L, TimeUnit.SECONDS)
                .schedule();

        logger.info("Start server because someone tried to join.");
    }

}
