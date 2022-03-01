package com.cooleg.antiscanserver;

import com.cooleg.antiscanserver.SQLUtils.SQLStorage;
import com.cooleg.antiscanserver.SQLUtils.SQLTableManager;
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

    public boolean checkIP(String ip) {
        boolean contains = false;
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT ip from IPs WHERE ip=?");
            statement.setString(1, ip);
            ResultSet has = statement.executeQuery();
            if (has.next()) {
                contains = true;
                System.out.println(has.getString("ip"));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            try { if(connection != null) connection.close(); } catch (SQLException exception){ exception.printStackTrace(); }
        }
        return contains;
    }

    public boolean checkUUID(String uuid){
        boolean contains = false;
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT uuid from UUIDs WHERE uuid=?");
            statement.setString(1, uuid);
            ResultSet has = statement.executeQuery();
            if (has.next()) {
                contains = true;
                System.out.println(has.getString("uuid"));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            try {if (connection != null) connection.close();} catch (SQLException exception) {exception.printStackTrace();}
        }
        return contains;
    }
}

