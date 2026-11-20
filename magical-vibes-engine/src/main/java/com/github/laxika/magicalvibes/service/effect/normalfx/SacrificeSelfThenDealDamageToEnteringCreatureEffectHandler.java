package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the entering-creature sacrifice trigger by reusing the sacrifice and damage handlers, the damage only if
 * the sacrifice happened, while binding the event's creature as the damage recipient.
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

        // "Sacrifice this creature. If you do, it deals N damage to that creature": the sacrifice is dispatched
        // within this step, so it departs at once and records its result before the damage is judged.
        SacrificeSelfEffect sacrifice = new SacrificeSelfEffect();
        EffectHandler sacrificeHandler = effectHandlerRegistry.getHandler(sacrifice);
        DealDamageToTargetCreatureEffect damage = new DealDamageToTargetCreatureEffect(e.damage());
        EffectHandler damageHandler = effectHandlerRegistry.getHandler(damage);
        if (sacrificeHandler == null || damageHandler == null) {
            return;
        }
        sacrificeHandler.resolve(gameData, entry, sacrifice);
        if (!entry.getResolutionResults().outcome(StepKind.SACRIFICE).happened()) {
            return;
        }

        var previousTargetId = entry.getTargetId();
        entry.setTargetIdForEffectResolution(entry.getTriggeringPermanentId());
        try {
            damageHandler.resolve(gameData, entry, damage);
        } finally {
            entry.restoreTargetIdAfterEffectResolution(previousTargetId);
        }
    }
}
