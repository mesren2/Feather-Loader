package com.featherloader.api;

/**
 * Information about a mod
 */
public interface ModInfo {
    /**
     * @return The mod ID
     */
    String id();

    /**
     * @return The mod name
     */
    String name();

    /**
     * @return The mod version
     */
    String version();

    /**
     * @return The mod description
     */
    String description();

    /**
     * @return The mod authors
     */
    String[] authors();
}