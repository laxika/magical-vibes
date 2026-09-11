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
 * source Aura currently enchants. If the Aura has left the battlefield, the captured attachment
 * supplies its last known enchanted creature.
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

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        var enchantedId = source == null ? entry.getTargetId() : source.getAttachedTo();
        if (enchantedId == null) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (creature == null) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, creature, e.counterType(), e.amount());
    }
}
