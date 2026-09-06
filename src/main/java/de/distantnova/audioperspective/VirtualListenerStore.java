package de.distantnova.audioperspective;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VirtualListenerStore {

    private static final long TIMEOUT_MILLIS = 5_000L;
    private static final Map<UUID, State> LISTENERS = new ConcurrentHashMap<>();

    private VirtualListenerStore() {
    }

    public static UpdateResult update(UUID playerId, ResourceKey<Level> dimension, Vec3 position) {
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) {
            return UpdateResult.REJECTED;
        }
        State previous = LISTENERS.put(playerId, new State(dimension, position, System.currentTimeMillis()));
        if (previous == null) {
            return UpdateResult.ENABLED;
        }
        if (previous.dimension().equals(dimension) && previous.position().equals(position)) {
            return UpdateResult.REFRESHED;
        }
        return UpdateResult.MOVED;
    }

    public static boolean clear(UUID playerId) {
        return LISTENERS.remove(playerId) != null;
    }

    public static Vec3 resolve(UUID playerId, ResourceKey<Level> dimension, Vec3 fallback) {
        State state = LISTENERS.get(playerId);
        if (state == null) {
            return fallback;
        }
        if (!state.dimension().equals(dimension) || System.currentTimeMillis() - state.lastUpdateMillis() > TIMEOUT_MILLIS) {
            LISTENERS.remove(playerId, state);
            return fallback;
        }
        return state.position();
    }

    public static List<UUID> expireStale() {
        long now = System.currentTimeMillis();
        return LISTENERS.entrySet().stream()
                .filter(entry -> now - entry.getValue().lastUpdateMillis() > TIMEOUT_MILLIS)
                .filter(entry -> LISTENERS.remove(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    private record State(ResourceKey<Level> dimension, Vec3 position, long lastUpdateMillis) {
    }

    public enum UpdateResult {
        ENABLED,
        MOVED,
        REFRESHED,
        REJECTED
    }
}
