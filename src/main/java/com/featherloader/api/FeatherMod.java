package com.featherloader.api;

public interface FeatherMod {
    /**
     * Called during the early initialization phase, before Minecraft starts
     */
    default void onPreInitialize() {}

    /**
     * Called when Minecraft is initializing
     */
    void onInitialize();

    /**
     * Called after Minecraft has finished initializing
     */
    default void onPostInitialize() {}

    /**
     * @return The mod's metadata
     */
    ModInfo getModInfo();
}