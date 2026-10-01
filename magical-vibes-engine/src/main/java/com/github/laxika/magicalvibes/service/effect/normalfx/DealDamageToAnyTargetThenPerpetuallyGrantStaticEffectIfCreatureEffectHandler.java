package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles any-target damage followed by a perpetual static effect for creature targets. */
@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DealDamageToAnyTargetEffectHandler damageHandler;
    private final PerpetuallyGrantStaticEffectToTargetCreatureEffectHandler staticEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var combined = (DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        damageHandler.resolve(gameData, entry, new DealDamageToAnyTargetEffect(combined.damage()));

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null && gameQueryService.isCreature(gameData, target)) {
            staticEffectHandler.resolve(gameData, entry,
                    new PerpetuallyGrantStaticEffectToTargetCreatureEffect(combined.staticEffect()));
        }
    }
}
