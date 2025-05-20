package com.featherloader.api.event;

import java.util.*;
import java.util.function.Consumer;

public class EventBus {
    private static final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

    public static <T> void register(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T> void post(T event) {
        List<Consumer<?>> eventListeners = listeners.getOrDefault(event.getClass(), Collections.emptyList());
        for (Consumer<?> listener : eventListeners) {
            ((Consumer<T>) listener).accept(event);
        }
    }
}