package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.GameRegistry;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.UUID;

public class TestGameRegistry extends GameRegistry {

    private static final VarHandle GAMES_HANDLE;

    static {
        try {
            GAMES_HANDLE = MethodHandles.privateLookupIn(GameRegistry.class, MethodHandles.lookup())
                    .findVarHandle(GameRegistry.class, "games", Map.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void reset() {
        ((Map<UUID, GameData>) GAMES_HANDLE.get(this)).clear();
    }
}
