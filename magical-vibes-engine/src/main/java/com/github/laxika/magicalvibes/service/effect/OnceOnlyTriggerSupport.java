package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;

/** Tracks triggered abilities that are limited to once for a permanent object. */
public final class OnceOnlyTriggerSupport {

    private OnceOnlyTriggerSupport() {
    }

    public static boolean isFired(GameData gameData, Permanent source) {
        return gameData.onceOnlyTriggersFired.contains(source.getId());
    }

    public static void mark(GameData gameData, Permanent source) {
        gameData.onceOnlyTriggersFired.add(source.getId());
    }

    public static CardEffect unwrapIfAvailable(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof OnceOnlyTriggerEffect once)) {
            return effect;
        }
        if (isFired(gameData, source)) {
            return null;
        }
        return once.wrapped();
    }

    public static void markIfNeeded(GameData gameData, Permanent source, CardEffect effect) {
        if (effect instanceof OnceOnlyTriggerEffect) {
            mark(gameData, source);
        }
    }
}
