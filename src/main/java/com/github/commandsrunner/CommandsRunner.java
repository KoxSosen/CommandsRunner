package com.github.commandsrunner;

import com.github.commandsrunner.utils.GlobalState;
import com.github.commandsrunner.utils.Util;
import com.google.inject.Inject;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(
        id = "commandsrunner",
        name = "CommandsRunner",
        authors = "KoxSosen",
        version = "1.1"
)
public class CommandsRunner {

    private final ProxyServer server;
    private final Util util;

    private final Logger logger;

    @Inject
    public CommandsRunner(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.util = new Util();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getScheduler()
                .buildTask(this, () -> util.runServerLogic(server, this, logger))
                .repeat(5, TimeUnit.MINUTES)
                .schedule();
    }

    @Subscribe
    public void onServerConnection(ServerPreConnectEvent serverPreConnectEvent) {
        // Start the server if it isn't running already, or it isn't starting already
        switch (GlobalState.getState()) {
            case RUNNING:
                break;
            case STARTING:
                serverPreConnectEvent.getPlayer().disconnect(Component.text("Server is starting, please wait."));
                break;
            case SHUTTINGDOWN:
                // Here someone joined while the server was shutting down. Meaning, we need to schedule a start, but wait for the previous
                // shutdown to finish. So, we set a delay of 40 seconds.
                serverPreConnectEvent.getPlayer().disconnect(Component.text("Server is shutting down, we will restart it for you shortly."));
                server.getScheduler()
                        .buildTask(this, () -> util.startServer(server, this, logger))
                        .delay(40L, TimeUnit.SECONDS)
                        .schedule();
                break;
            case STOPPED:
                serverPreConnectEvent.getPlayer().disconnect(Component.text("Server is stopped, we are starting it for you."));
                util.startServer(server, this, logger);
                break;
            default:
                break;
        }
    }
}
