package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;

public final class SurvivalTriggerSupport {

    private SurvivalTriggerSupport() {
    }

    /**
     * Unwraps a Survival trigger, or returns {@code null} when the current postcombat main phase
     * isn't the turn's second main phase (a main phase after an additional combat is a later one).
     */
    public static CardEffect unwrapIfNotEvaluated(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof SurvivalTriggerEffect survival)) {
            return effect;
        }
        if (gameData.combatPhasesThisTurn > 1) {
            return null;
        }
        return survival.wrapped();
    }

}
