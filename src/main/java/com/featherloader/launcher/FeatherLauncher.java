package com.featherloader.launcher;

import com.featherloader.api.FeatherMod;
import com.featherloader.core.FeatherLoader;

import java.io.File;
import java.util.logging.Logger;

public class FeatherLauncher {
    private static final Logger LOGGER = Logger.getLogger("FeatherLoader");

    public static void main(String[] args) {
        try {
            // Initialize FeatherLoader early
            LOGGER.info("Initializing FeatherLoader for Minecraft 1.21+");

            // Get game directory
            File gameDir = new File(".");
            boolean mixinsEnabled = Boolean.getBoolean("featherloader.mixins.enabled");

            // Initialize FeatherLoader
            FeatherLoader.initialize(gameDir, mixinsEnabled);

            // Pre-initialize mods
            for (FeatherMod mod : FeatherLoader.getLoadedMods()) {
                try {
                    LOGGER.info("Pre-initializing mod: " + mod.getModInfo().name());
                    mod.onPreInitialize();
                } catch (Exception e) {
                    LOGGER.severe("Failed to pre-initialize mod: " + mod.getModInfo().name());
                    e.printStackTrace();
                }
            }

            // Launch vanilla Minecraft with our additions
            LOGGER.info("Launching Minecraft...");
            Class<?> mainClass = Class.forName("net.minecraft.client.main.Main");
            mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Failed to find Minecraft main class. Make sure FeatherLoader is properly installed.");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize FeatherLoader:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}