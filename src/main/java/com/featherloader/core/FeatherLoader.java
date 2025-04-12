package com.featherloader.core;

import com.featherloader.api.FeatherMod;
import com.featherloader.mixin.FeatherMixinBootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * FeatherLoader - A lightweight Minecraft modloader for 1.21+
 */
public class FeatherLoader {
    private static final Logger LOGGER = Logger.getLogger("FeatherLoader");
    private static final List<FeatherMod> loadedMods = new ArrayList<>();
    private static final Map<String, Object> sharedResources = new HashMap<>();

    private static boolean initialized = false;
    private static boolean mixinsEnabled = false;

    /**
     * Initialize the mod loader
     * @param gameDir The game directory
     * @param mixinsEnabled Whether to enable mixin support
     */
    public static void initialize(File gameDir, boolean mixinsEnabled) {
        if (initialized) {
            LOGGER.warning("FeatherLoader already initialized!");
            return;
        }

        LOGGER.info("Initializing FeatherLoader for Minecraft 1.21+");
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

        // Initialize mods
        for (FeatherMod mod : loadedMods) {
            try {
                LOGGER.info("Initializing mod: " + mod.getModInfo().name() + " v" + mod.getModInfo().version());
                mod.onInitialize();
            } catch (Exception e) {
                LOGGER.severe("Failed to initialize mod: " + mod.getModInfo().name());
                e.printStackTrace();
            }
        }

        initialized = true;
        LOGGER.info("FeatherLoader initialized with " + loadedMods.size() + " mods");
    }

    /**
     * Get all loaded mods
     * @return A list of all loaded mods
     */
    public static List<FeatherMod> getLoadedMods() {
        return new ArrayList<>(loadedMods);
    }

    /**
     * Check if mixin support is enabled
     * @return True if mixin support is enabled
     */
    public static boolean isMixinsEnabled() {
        return mixinsEnabled;
    }

    /**
     * Share a resource with other mods
     * @param key The resource key
     * @param value The resource value
     */
    public static void shareResource(String key, Object value) {
        sharedResources.put(key, value);
    }

    /**
     * Get a shared resource
     * @param key The resource key
     * @return The resource value, or null if not found
     */
    public static Object getSharedResource(String key) {
        return sharedResources.get(key);
    }
}