package com.featherloader.mixin;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.mixin.Mixins;

import java.util.logging.Logger;

public class FeatherMixinBootstrap {
    private static final Logger LOGGER = Logger.getLogger("FeatherLoader");
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            LOGGER.info("Initializing Mixin support");

            MixinBootstrap.init();
            initialized = true;
            LOGGER.info("Mixin support initialized");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize Mixin support");
            e.printStackTrace();
        }
    }

    public static void addConfiguration(String config) {
        if (!initialized) {
            LOGGER.warning("Mixin support not initialized, cannot add configuration");
            return;
        }

        LOGGER.info("Adding mixin configuration: " + config);
        Mixins.addConfiguration(config);
    }
}
