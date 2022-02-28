package com.cooleg.antiscanserver.SQLUtils;

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ISQLStorage {
    abstract void createDatabase() throws SQLException;
    abstract void createTables(Plugin plugin) throws SQLException;
    abstract void connectToDatabase();
    abstract Connection getConnection() throws SQLException;
}
