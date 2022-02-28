package com.cooleg.antiscanserver;

import com.cooleg.antiscanserver.SQLUtils.SQLStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

public class AsyncJoinListener implements Listener {
    private SQLLogger sqlLogger;

    public AsyncJoinListener(SQLLogger sqlLogger) {
        this.sqlLogger = sqlLogger;
    }

    @EventHandler
    public void AsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {

        String uuid = e.getUniqueId().toString();
        String ip = e.getAddress().getHostAddress();

        sqlLogger.checkUUID(uuid, e);
        sqlLogger.checkIP(ip, e);

    }

}
