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
        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) {
            return null;
        }
        return once.wrapped();
    }

    public static void markIfNeeded(GameData gameData, Permanent source, CardEffect effect) {
        if (effect instanceof OncePerTurnTriggerEffect) {
            gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
        }
    }
}
