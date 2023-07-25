package com.github.commandsrunner;

import com.github.commandsrunner.utils.GlobalState;
import com.github.commandsrunner.utils.State;
import com.github.commandsrunner.utils.Util;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

@Plugin(
        id = "commandsrunner",
        name = "CommandsRunner",
        version = "1.0"
)
public class CommandsRunner {

    private final ProxyServer server;
    private final Plugin plugin;
    private final Util util;

    @Inject
    public CommandsRunner(ProxyServer server, Plugin plugin) {
        this.server = server;
        this.plugin = plugin;
        this.util = new Util();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getScheduler()
                .buildTask(plugin, () -> util.runServerLogic(server, plugin))
                .repeat(5L, TimeUnit.MINUTES)
                .schedule();
    }

    @Subscribe
    public void onServerConnection(ServerConnection serverConnection) {
        // Start the server if it isn't running already, or it isn't starting already
        switch (GlobalState.getState()) {
            case RUNNING:
                break;
            case STARTING:
                serverConnection.getPlayer().disconnect(Component.text("Server is starting, please wait."));
                break;
            case SHUTTINGDOWN:
                // Here someone joined while the server was shutting down. Meaning, we need to schedule a start, but wait for the previous
                // shutdown to finish. So, we set a delay of 40 seconds.
                serverConnection.getPlayer().disconnect(Component.text("Server is shutting down, we will restart it for you shortly."));
                server.getScheduler()
                        .buildTask(plugin, () -> {
                            util.startServer(server, plugin);
                        })
                        .delay(40L, TimeUnit.SECONDS)
                        .schedule();
                break;
            case STOPPED:
                serverConnection.getPlayer().disconnect(Component.text("Server is stopped, we are starting it for you."));
                util.startServer(server, plugin);
                break;
            default:
                break;
        }
    }
}
