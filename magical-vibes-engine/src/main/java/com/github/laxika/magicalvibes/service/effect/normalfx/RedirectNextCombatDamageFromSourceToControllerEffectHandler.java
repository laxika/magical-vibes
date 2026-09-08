package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.SourceNextCombatDamageToControllerShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageFromSourceToControllerEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RedirectNextCombatDamageFromSourceToControllerEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextCombatDamageFromSourceToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID controllerId = entry.getControllerId();
        if (sourcePermanentId == null || controllerId == null) {
            return;
        }

        gameData.sourceNextCombatDamageToControllerShields.add(
                new SourceNextCombatDamageToControllerShield(sourcePermanentId, controllerId));
    }
}
