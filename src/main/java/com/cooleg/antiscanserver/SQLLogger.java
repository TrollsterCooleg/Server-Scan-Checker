package com.cooleg.antiscanserver;

import com.cooleg.antiscanserver.SQLUtils.SQLStorage;
import com.cooleg.antiscanserver.SQLUtils.SQLTableManager;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLLogger extends SQLStorage {

    public SQLLogger(Plugin main, CustomConfig sqlSettings) throws SQLException {
        super(main, sqlSettings);
    }

    @Override
    public void createTables(Plugin plugin) throws SQLException {
        Connection connection = getConnection();
        addTable(connection, new SQLTableManager("IPs", "PRIMARY KEY (ip)", "ip VARCHAR(15)"));
        addTable(connection, new SQLTableManager("UUIDs", "PRIMARY KEY (uuid)", "uuid VARCHAR(36)"));
        connection.close();
    }

    public void checkIP(String ip, AsyncPlayerPreLoginEvent e){
        Bukkit.getScheduler().runTaskAsynchronously(main, ()->{
            Connection connection = null;
            try {
                connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT ip from IPs WHERE ip=?");
                statement.setString(1, ip);
                ResultSet has = statement.executeQuery();
                if (has.next()) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Caught in the trap.");
                    System.out.println(has.getString("ip"));
                } else {
                    e.allow();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            finally {
                try { if(connection != null) connection.close(); } catch (SQLException exception){ exception.printStackTrace(); }
            }
        });
    }

    public boolean checkUUID(String uuid, AsyncPlayerPreLoginEvent e){
        final boolean[] contains = new boolean[1];
        Bukkit.getScheduler().runTaskAsynchronously(main, ()->{
            Connection connection = null;
            try {
                connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT uuid from UUIDs WHERE uuid=?");
                statement.setString(1, uuid);
                ResultSet has = statement.executeQuery();
                if (has.next()) {
                    System.out.println(has.getString("uuid"));
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Caught in the trap.");
                } else {
                    e.allow();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            finally {
                try { if(connection != null) connection.close(); } catch (SQLException exception){ exception.printStackTrace(); }
            }
        });
        if (contains[0] = true) {return true;} else {return false;}
    }

}

