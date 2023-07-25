package com.github.commandsrunner.utils;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Util {

    public void executeCommands(List<String> commands) {
        for (String command : commands) {
            final Process process;
            try {
                process = Runtime.getRuntime().exec(command);
                process.waitFor();
                process.destroy();
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

    public void runServerLogic(ProxyServer proxyServer, Plugin plugin) {
        // If there are more than 0 players online, the server should be running.
        // If there is less than 1, that means that we can shut down.
        if (!isEnoughPlayers(proxyServer)) {
            runShutdownCommands();
            GlobalState.setState(State.SHUTTINGDOWN);

            // It shouldn't take more than 20 seconds for the server to shut down
            // Meaning we can set its tate to stopped
            proxyServer.getScheduler()
                    .buildTask(plugin, () -> GlobalState.setState(State.STOPPED))
                    .delay(20L, TimeUnit.SECONDS)
                    .schedule();
        } else {
            GlobalState.setState(State.RUNNING);
        }
    }

    public void startServer(ProxyServer proxyServer, Plugin plugin) {
        runStartupCommands();
        GlobalState.setState(State.STARTING);

        // It shouldn't take more than 20 seconds for the server to start
        // Meaning we can set its tate to running
        proxyServer.getScheduler()
                .buildTask(plugin, () -> GlobalState.setState(State.RUNNING))
                .delay(20L, TimeUnit.SECONDS)
                .schedule();
    }

}
