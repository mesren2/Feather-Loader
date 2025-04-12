package com.featherloader.api;

/**
 * Base interface for all FeatherLoader mods
 */
public interface FeatherMod {
    /**
     * Called when the mod is being initialized
     */
    void onInitialize();

    /**
     * @return The mod's metadata
     */
    ModInfo getModInfo();
}