package com.featherloader.core;

import com.featherloader.api.FeatherMod;
import com.featherloader.api.ModInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class ModDiscoverer {
    private static final Logger LOGGER = Logger.getLogger("FeatherLoader");

    public static void discoverAndLoadMods(File modsDir, List<FeatherMod> loadedMods) {
        File[] modFiles = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (modFiles == null || modFiles.length == 0) {
            LOGGER.info("No mods found in " + modsDir.getAbsolutePath());
            return;
        }

        for (File modFile : modFiles) {
            try {
                loadMod(modFile, loadedMods);
            } catch (Exception e) {
                LOGGER.severe("Failed to load mod: " + modFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static void loadMod(File modFile, List<FeatherMod> loadedMods) throws IOException, ReflectiveOperationException {
        LOGGER.info("Loading mod from: " + modFile.getName());

        JarFile jarFile = new JarFile(modFile);
        JarEntry modInfoEntry = jarFile.getJarEntry("feather-mod.properties");

        if (modInfoEntry == null) {
            LOGGER.warning("Jar file " + modFile.getName() + " does not contain feather-mod.properties, skipping");
            jarFile.close();
            return;
        }

        // Load mod info
        Properties modProperties = new Properties();
        modProperties.load(jarFile.getInputStream(modInfoEntry));

        String mainClass = modProperties.getProperty("main-class");
        if (mainClass == null || mainClass.isEmpty()) {
            LOGGER.warning("feather-mod.properties in " + modFile.getName() + " does not specify main-class, skipping");
            jarFile.close();
            return;
        }

        // Create class loader and load main class
        URL[] urls = { modFile.toURI().toURL() };
        FeatherClassLoader classLoader = new FeatherClassLoader(urls, ModDiscoverer.class.getClassLoader());

        Class<?> clazz = classLoader.loadClass(mainClass);
        if (!FeatherMod.class.isAssignableFrom(clazz)) {
            LOGGER.warning("Main class " + mainClass + " in " + modFile.getName() + " does not implement FeatherMod, skipping");
            jarFile.close();
            return;
        }

        // Instantiate mod
        FeatherMod mod = (FeatherMod) clazz.getDeclaredConstructor().newInstance();
        loadedMods.add(mod);

        LOGGER.info("Loaded mod: " + mod.getModInfo().name() + " v" + mod.getModInfo().version());

        jarFile.close();
    }
}