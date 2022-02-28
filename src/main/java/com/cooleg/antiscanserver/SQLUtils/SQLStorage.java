package com.cooleg.antiscanserver.SQLUtils;

import com.cooleg.antiscanserver.CustomConfig;

import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.sql.*;
import java.util.*;


public abstract class SQLStorage extends ISQLStorage {
    protected static HashMap<String, SQLTableManager> tables = new HashMap<>();
    protected Plugin main;
    public static boolean connected = false;

    public HikariDataSource hikari;

    protected final CustomConfig sqlSettings;

    public SQLStorage(Plugin main, CustomConfig sqlSettings){
        this.main = main;
        this.sqlSettings = sqlSettings;
    }

    public boolean databaseIsEmpty(){
        try {
            String database = sqlSettings.getString("database");
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(DISTINCT `table_name`) FROM `information_schema`.`columns` WHERE `table_schema` = ?");
            statement.setString(1, database);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            connection.close();
            return resultSet.getInt(1) == 0;
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static SQLTableManager getTableManager(String table){
        return tables.get(table);
    }

    protected static void addTable(Connection connection, SQLTableManager tableManager) throws SQLException{
        tables.put(tableManager.getTable(), tableManager);
        ArrayList<String> columns = tableManager.getColumns();
        if(tableExists(connection, tableManager.getTable())) {


            PreparedStatement checkColumn = connection.prepareStatement(String.format("SHOW COLUMNS FROM %s", tableManager.getTable()));
            ResultSet rs = checkColumn.executeQuery();
            HashMap<String, String> columnList = new HashMap<>();
            while (rs.next()) columnList.put(rs.getString("Field"), rs.getString("Type"));
            for(String columnFull : columns){
                String[] columnSplit = columnFull.split(" ");
                String column = columnSplit[0];
                String type = columnSplit[1];


                if(!columnList.containsKey(column)){
                    PreparedStatement addColumn = connection.prepareStatement(String.format("ALTER TABLE %s ADD %s", tableManager.getTable(), columnFull));
                    addColumn.executeUpdate();
                    Bukkit.getLogger().info("[MySQL] §cAdded missing column §6" + column);
                }
                else {
                    String typeInDB = columnList.get(column);
                    if(!typeInDB.equalsIgnoreCase(type)){
                        PreparedStatement changeType = connection.prepareStatement((String.format("ALTER TABLE %s MODIFY %s %s", tableManager.getTable(), column, type)));
                        changeType.executeUpdate();
                        Bukkit.getLogger().info("[MySQL] §cChanged column §6" + column + " to type " + type + "in table " + tableManager.getTable());
                    }
                }
            }
            rs.close();
            return;
        };
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("CREATE TABLE ");
        tableBuilder.append(tableManager.getTable());
        tableBuilder.append("(");
        for(int i = 0; i < columns.size(); i++) {
            String c = columns.get(i);
            tableBuilder.append(c);
            if(i != columns.size()-1) tableBuilder.append(",");
        }
        if(!tableManager.getPrimaryKey().isEmpty()) tableBuilder.append(",");
        tableBuilder.append(tableManager.getPrimaryKey());
        tableBuilder.append(")");
        PreparedStatement statement = connection.prepareStatement(tableBuilder.toString());
        statement.execute();
        statement.close();
        Bukkit.getLogger().info("§6[MySQL] §aTable " + tableManager.getTable() + " created!");
    }

    public static boolean tableExists(Connection connection, String name) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SHOW TABLES LIKE ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        boolean exists = resultSet.next();
        resultSet.close();
        return exists;
    }

    public void createDatabase() throws SQLException{
        if(databaseIsEmpty()){
            createTables(main);
        }
        else {
            //Check for columns and such
            createTables(main);
        }
    }

    @Override
    public abstract void createTables(Plugin plugin) throws SQLException;

    protected void setupParameters(HikariDataSource hikari){
        hikari.addDataSourceProperty("cachePrepStmts", true);
        hikari.addDataSourceProperty("prepStmtCacheSize", 250);
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikari.addDataSourceProperty("useServerPrepStmts", true);
        hikari.addDataSourceProperty("useLocalSessionState", true);
        hikari.addDataSourceProperty("rewriteBatchedStatements", true);
        hikari.addDataSourceProperty("cacheResultSetMetadata", true);
        hikari.addDataSourceProperty("cacheServerConfiguration", true);
        hikari.addDataSourceProperty("elideSetAutoCommits", true);
        hikari.addDataSourceProperty("maintainTimeStats", false);
        hikari.setConnectionTimeout(5000);
    }

    public void connectToDatabase() {
        String database = sqlSettings.getString("database");
        if(database.equals("notset")) {
            Bukkit.getLogger().info("Please modify your SQL config to continue.");
            Bukkit.getPluginManager().disablePlugin(main);
            return;
        }
        String host = sqlSettings.getString("host");
        String port = sqlSettings.getString("port");
        String username = sqlSettings.getString("username");
        String password = sqlSettings.getString("password");
        Bukkit.getScheduler().runTaskAsynchronously(main, ()->{
            hikari = new HikariDataSource();
            hikari.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8", host, port, database));
            setupParameters(hikari);
            hikari.setUsername(username);
            hikari.setPassword(password);
            try {
                createDatabase();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            connected = true;
        });
    }

    public void truncateTable(String table){
        Bukkit.getScheduler().runTaskAsynchronously(main, ()->{
            Connection connection = null;
            try {
                connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(String.format("TRUNCATE TABLE %s", table));
                statement.executeUpdate();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                try { if(connection != null) connection.close(); } catch (SQLException e){ e.printStackTrace(); }
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    public Object getValue(String column, String table , String lookupColumn, String lookupValue){
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(String.format("SELECT %s FROM %s WHERE %s=?", column, table, lookupColumn));
            statement.setString(1, lookupValue);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                return null;
            }
            return rs.getObject(column);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try { if(connection != null) connection.close(); } catch (SQLException e){ e.printStackTrace(); }
        }
        return null;
    }

    public Object getValue(String column, String table){
        return getValue(column, table, "1", "1");
    }

    public void updateValue(String table, String column, String lookupColumn, Object lookupValue, Object value){
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            updateValueSync(table,column,lookupColumn,lookupValue,value);
        });
    }
    public void updateValue(String table, String column, Object value){
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            updateValueSync(table,column,value);
        });
    }
    public void updateValueSync(String table, String column, Object value){
        updateValueSync(table,column, "1", "1", value);
    }
    public void updateValueSync(String table, String column, String lookupColumn, Object lookupValue, Object value){
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(String.format("UPDATE %s SET %s=? WHERE %s=?", table,column,lookupColumn));

            statement.setObject(1, value);
            statement.setObject(2, lookupValue);
            statement.executeUpdate();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try { if(connection != null) connection.close(); } catch (SQLException e){ e.printStackTrace(); }
        }
    }
    public void deleteValue(String table, String lookupColumn, Object lookupValue){
        Bukkit.getScheduler().runTaskAsynchronously(main, ()->{
            Connection connection = null;
            try {
                connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(String.format("DELETE FROM %s WHERE %s=?", table,lookupColumn));
                statement.setObject(1, lookupValue);
                statement.executeUpdate();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                try { if(connection != null) connection.close(); } catch (SQLException e){ e.printStackTrace(); }
            }
        });
    }

}
