package com.cooleg.antiscanserver;

import org.bukkit.plugin.java.JavaPlugin;

import com.cooleg.antiscanserver.SQLUtils.SQLStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.sql.SQLException;

public final class Main extends JavaPlugin {

    PluginManager pm = Bukkit.getPluginManager();
    static SQLStorage sqlDatabase = null;

    @Override
    public void onEnable() {
        CustomConfig Config = new CustomConfig(this, "sql.yml");
        try {
            Config.load(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            sqlDatabase = new SQLLogger(this, Config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sqlDatabase.connectToDatabase();
        pm.registerEvents(new AsyncJoinListener((SQLLogger) sqlDatabase), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}

