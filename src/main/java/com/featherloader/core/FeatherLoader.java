package com.featherloader.core;

import com.featherloader.api.FeatherMod;
import com.featherloader.mixin.FeatherMixinBootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FeatherLoader {
    private static final Logger LOGGER = Logger.getLogger("FeatherLoader");
    private static final List<FeatherMod> loadedMods = new ArrayList<>();
    private static final Map<String, Object> sharedResources = new HashMap<>();

    private static boolean initialized = false;
    private static boolean mixinsEnabled = false;

    public static void initialize(File gameDir, boolean mixinsEnabled) {
        if (initialized) {
            LOGGER.warning("FeatherLoader already initialized!");
            return;
        }

        LOGGER.info("Setting up FeatherLoader environment...");
        FeatherLoader.mixinsEnabled = mixinsEnabled;

        if (mixinsEnabled) {
            LOGGER.info("Mixin support enabled");
            FeatherMixinBootstrap.initialize();
        }

        // Create mods directory if it doesn't exist
        File modsDir = new File(gameDir, "feather-mods");
        if (!modsDir.exists()) {
            modsDir.mkdirs();
            LOGGER.info("Created mods directory at " + modsDir.getAbsolutePath());
        }

        // Load mods
        ModDiscoverer.discoverAndLoadMods(modsDir, loadedMods);

        initialized = true;
        LOGGER.info("FeatherLoader initialized with " + loadedMods.size() + " mods");
    }

    public static void finishInitialization() {
        // Call post-initialization for all mods
        for (FeatherMod mod : loadedMods) {
            try {
                LOGGER.info("Post-initializing mod: " + mod.getModInfo().name());
                mod.onPostInitialize();
            } catch (Exception e) {
                LOGGER.severe("Failed to post-initialize mod: " + mod.getModInfo().name());
                e.printStackTrace();
            }
        }
    }

    public static List<FeatherMod> getLoadedMods() {
        return new ArrayList<>(loadedMods);
    }

    public static boolean isMixinsEnabled() {
        return mixinsEnabled;
    }

    public static void shareResource(String key, Object value) {
        sharedResources.put(key, value);
    }

    public static Object getSharedResource(String key) {
        return sharedResources.get(key);
    }
}