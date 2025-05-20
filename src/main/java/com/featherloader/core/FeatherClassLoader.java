package com.featherloader.core;

import java.net.URL;
import java.net.URLClassLoader;

public class FeatherClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    public FeatherClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // If it's a Feather class, delegate to parent
        if (name.startsWith("com.featherloader.api") || name.startsWith("com.featherloader.core")) {
            return super.loadClass(name, resolve);
        }

        // Try to load from this class loader first
        try {
            c = findClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (ClassNotFoundException e) {
            // If not found, delegate to parent
            return super.loadClass(name, resolve);
        }
    }
}