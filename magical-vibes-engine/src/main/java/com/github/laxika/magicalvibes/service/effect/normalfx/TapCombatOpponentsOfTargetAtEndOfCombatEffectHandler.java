package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.TapCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentsOfTargetAtEndOfCombatEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the one-shot combat-opponent tap effect by recording each chosen creature. */
@Component
public class TapCombatOpponentsOfTargetAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapCombatOpponentsOfTargetAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID targetId : entry.targetsForEffect(effect)) {
            gameData.queueDelayedAction(new TapCombatOpponentsAtEndOfCombat(targetId));
        }
    }
}
