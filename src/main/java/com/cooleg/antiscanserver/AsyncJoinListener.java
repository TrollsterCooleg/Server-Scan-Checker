package com.cooleg.antiscanserver;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncJoinListener implements Listener {
    public SQLLogger sqlLogger;

    public AsyncJoinListener(SQLLogger sqlLogger) {
        this.sqlLogger = sqlLogger;
    }

    @EventHandler
    public void AsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {

        String uuid = e.getUniqueId().toString();
        String ip = e.getAddress().getHostAddress();

        boolean hasIp = sqlLogger.checkUUID(uuid);
        boolean hasUUID = sqlLogger.checkIP(ip);
        if (hasIp || hasUUID) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Caught in the trap.");
        } else {
            e.allow();
        }

    }

}
