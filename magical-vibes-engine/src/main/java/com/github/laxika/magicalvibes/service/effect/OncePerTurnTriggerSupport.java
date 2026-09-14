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

    public static void markIfNeeded(GameData gameData, Permanent source, CardEffect effect) {
        if (effect instanceof OncePerTurnTriggerEffect once && !once.markOnAcceptance()) {
            if (once.key() == null) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
            } else {
                gameData.keyedOncePerTurnTriggersFiredThisTurn
                        .computeIfAbsent(source.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                        .add(once.key());
            }
        }
    }

    private static boolean isFired(GameData gameData, Permanent source, OncePerTurnTriggerEffect effect) {
        if (effect.key() == null) {
            return gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId());
        }
        return gameData.keyedOncePerTurnTriggersFiredThisTurn
                .getOrDefault(source.getId(), java.util.Set.of())
                .contains(effect.key());
    }
}
