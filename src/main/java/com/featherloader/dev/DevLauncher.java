package com.featherloader.dev;

import com.featherloader.core.FeatherLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DevLauncher {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting FeatherLoader in development mode...");

        // Path to your Minecraft installation
        File minecraftDir = new File(System.getProperty("featherloader.minecraft.dir", ".minecraft"));

        // Initialize FeatherLoader
        boolean enableMixins = Boolean.parseBoolean(System.getProperty("featherloader.mixins", "true"));
        FeatherLoader.initialize(minecraftDir, enableMixins);

        // Add development mods
        addDevMods();

        // Launch Minecraft
        launchMinecraft(args);
    }

    private static void addDevMods() throws Exception {
        // Path to development mods (your workspace)
        String devModPaths = System.getProperty("featherloader.dev.mods", "");
        if (devModPaths.isEmpty()) {
            System.out.println("No development mods specified");
            return;
        }

        for (String path : devModPaths.split(File.pathSeparator)) {
            File modDir = new File(path);
            System.out.println("Loading development mod from: " + modDir.getAbsolutePath());

            // Add the mod's classes to the classpath
            File classesDir = new File(modDir, "build/classes/java/main");
            if (classesDir.exists()) {
                addToClasspath(classesDir.toURI().toURL());
            }

            // Load the mod's resources
            File resourcesDir = new File(modDir, "build/resources/main");
            if (resourcesDir.exists()) {
                addToClasspath(resourcesDir.toURI().toURL());
            }

            // Find and load the mod class
            File modPropertiesFile = new File(resourcesDir, "feather-mod.properties");
            if (modPropertiesFile.exists()) {
                // Read properties to get main class
                // Load and initialize the mod
                // ...
            }
        }
    }

    private static void addToClasspath(URL url) throws Exception {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, url);
    }

    private static void launchMinecraft(String[] args) throws Exception {
        // Launch Minecraft with the existing args
        Class<?> minecraftMainClass = Class.forName("net.minecraft.client.main.Main");
        Method mainMethod = minecraftMainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }
}