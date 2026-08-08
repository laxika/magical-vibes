package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutCountersOnEnchantedCreatureEffect}: places the counters on the creature the
 * source Aura was attached to. The creature id was captured onto the stack entry at activation
 * (see {@code AttachedPermanentSelfTargetingEffect}), so a bounce or sacrifice cost that already
 * detached the Aura does not stop the counters from landing.
 */
@Component
@RequiredArgsConstructor
public class PutCountersOnEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnEnchantedCreatureEffect) effect;

        if (entry.getTargetId() == null) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (creature == null) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, creature, e.counterType(), e.amount());
    }
}
