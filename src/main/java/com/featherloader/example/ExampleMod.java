package com.featherloader.example;

import com.featherloader.api.FeatherMod;
import com.featherloader.api.ModInfo;

import java.util.logging.Logger;

/**
 * Example mod for FeatherLoader
 */
public class ExampleMod implements FeatherMod {
    private static final Logger LOGGER = Logger.getLogger("FeatherExample");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing ExampleMod - Hello, Minecraft!");
    }

    @Override
    public ModInfo getModInfo() {
        return new ModInfo() {
            @Override
            public String id() {
                return "feather-example";
            }

            @Override
            public String name() {
                return "FeatherLoader Example Mod";
            }

            @Override
            public String version() {
                return "1.0.0";
            }

            @Override
            public String description() {
                return "An example mod for FeatherLoader";
            }

            @Override
            public String[] authors() {
                return new String[]{"FeatherLoader Team"};
            }
        };
    }
}