package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;

public final class SurvivalTriggerSupport {

    private SurvivalTriggerSupport() {
    }

    public static CardEffect unwrapIfNotEvaluated(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof SurvivalTriggerEffect survival)) {
            return effect;
        }
        if (!gameData.survivalTriggersEvaluated.add(source.getId())) {
            return null;
        }
        return survival.wrapped();
    }

}
