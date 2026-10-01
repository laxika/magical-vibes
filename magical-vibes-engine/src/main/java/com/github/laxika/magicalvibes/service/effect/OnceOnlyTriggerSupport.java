package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;

public final class OnceOnlyTriggerSupport {

    private OnceOnlyTriggerSupport() {
    }

    public static CardEffect unwrapIfAvailable(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof OnceOnlyTriggerEffect once)) {
            return effect;
        }
        if (gameData.onceOnlyTriggersFired.contains(source.getId())) {
            return null;
        }
        return once.wrapped();
    }

    public static void markIfNeeded(GameData gameData, Permanent source, CardEffect effect) {
        if (effect instanceof OnceOnlyTriggerEffect) {
            gameData.onceOnlyTriggersFired.add(source.getId());
        }
    }
}
