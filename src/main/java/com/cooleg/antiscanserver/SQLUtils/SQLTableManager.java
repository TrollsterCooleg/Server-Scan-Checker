package com.cooleg.antiscanserver.SQLUtils;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class SQLTableManager {
    private final LinkedHashMap<String, String> columns = new LinkedHashMap<>();
    private final String table;
    private final String primaryKeyName;
    private final String primaryKey;
    public SQLTableManager(String table, String primaryKey, String... columns){
        this.table = table;
        this.primaryKey = primaryKey;
        this.primaryKeyName = !primaryKey.isEmpty() ? primaryKey.split("\\(")[1].replace(")", "").trim() : "";
        for(String column : columns){
            String[] columnSplit = column.split(" ");
            String columnName = columnSplit[0].trim();
            this.columns.put(columnName, column);
        }
    }

    public void getValuesFromPrimaryKey(SQLStorage storage,String primaryKeyLookup, Consumer<HashMap<String, Object>> values){
        if(this.primaryKeyName.isEmpty()) throw new IllegalArgumentException("Primary key must be specified.");
        Connection connection = null;
        HashMap<String, Object> objectHashMap = new HashMap<>();
        try {
            connection = storage.getConnection();
            PreparedStatement statement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s=?", table, this.primaryKeyName));
            statement.setString(1, primaryKeyLookup);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()){
                values.accept(objectHashMap);
                return;
            }
            for(String column : columns.keySet()){
                objectHashMap.put(column, resultSet.getObject(column));
            }
            values.accept(objectHashMap);
        }
        catch (SQLException e){
            e.printStackTrace();
            values.accept(objectHashMap);
        }
        finally {
            try { if(connection != null) connection.close(); } catch (SQLException e){ e.printStackTrace(); }
        }
    }

    public String getTable() {
        return table;
    }

    public ArrayList<String> getColumns(){
        return new ArrayList<>(columns.values());
    }

    public String getPrimaryKey() {
        return primaryKey;
    }
}
