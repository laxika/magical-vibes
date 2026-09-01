package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the entering-creature sacrifice trigger by reusing the standard sacrifice contingency
 * and damage handlers while binding the event's creature as the damage recipient.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfThenDealDamageToEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfThenDealDamageToEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfThenDealDamageToEnteringCreatureEffect) effect;
        if (entry.getTriggeringPermanentId() == null) {
            return;
        }

        SacrificeSelfThenEffect materialized = new SacrificeSelfThenEffect(
                new DealDamageToTargetCreatureEffect(e.damage()));
        EffectHandler handler = effectHandlerRegistry.getHandler(materialized);
        if (handler == null) {
            return;
        }

        var previousTargetId = entry.getTargetId();
        entry.setTargetIdForEffectResolution(entry.getTriggeringPermanentId());
        try {
            handler.resolve(gameData, entry, materialized);
        } finally {
            entry.restoreTargetIdAfterEffectResolution(previousTargetId);
        }
    }
}
