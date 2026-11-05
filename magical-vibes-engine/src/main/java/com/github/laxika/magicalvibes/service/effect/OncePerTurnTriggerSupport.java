package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

public final class OncePerTurnTriggerSupport {

    private OncePerTurnTriggerSupport() {
    }

    public static CardEffect unwrapIfAvailable(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof OncePerTurnTriggerEffect once)) {
            return effect;
        }
        if (isFired(gameData, source, once)) {
            return null;
        }
        return once.wrapped();
    }

    /** Marks {@code source} as having triggered its {@link OncePerTurnTriggerEffect} ability this turn. */
    public static void markIfNeeded(GameData gameData, Permanent source, CardEffect effect) {
        if (effect instanceof OncePerTurnTriggerEffect) {
            gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
        }
    }

    public static boolean isFired(GameData gameData, Permanent source, OncePerTurnTriggerEffect effect) {
        return gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId());
    }
}
