package com.cooleg.antiscanserver;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CustomConfig extends YamlConfiguration {
    protected final Plugin plugin;
    protected final File file;

    public CustomConfig(Plugin plugin, String path) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), path);
    }

    public void load() {
        try {
            load(file);
        }
        catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

    public void load(Plugin main) throws Exception {
        if(!file.exists()){
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }
        load(file);
        loadDefaults(file.getName(), main);
        load(file);
    }

    public void loadDefaults(String path, Plugin plugin) throws Exception{
        InputStream in = plugin.getClass().getResourceAsStream( "/" + path);
        FileConfiguration defaults = loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        for(String key : defaults.getKeys(false)){
            if(!contains(key)){
                set(key, defaults.get(key));
            }
        }
        save(file);
    }

    public void save() throws IOException {
        if(!file.exists()) {
            if(!file.createNewFile()) return;
        }
        save(file);
    }
}
