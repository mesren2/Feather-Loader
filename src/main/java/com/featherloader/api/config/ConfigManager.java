package com.featherloader.api.config;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private final File configDir;

    public ConfigManager(File gameDir) {
        this.configDir = new File(gameDir, "feather-config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public Properties loadConfig(String modId) {
        Properties properties = new Properties();
        File configFile = new File(configDir, modId + ".properties");

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return properties;
    }

    public void saveConfig(String modId, Properties properties) {
        File configFile = new File(configDir, modId + ".properties");

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "Configuration for " + modId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}