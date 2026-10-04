package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnEnchantedCreatureEffect) effect;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceSnapshot = source == null ? entry.getSourcePermanentSnapshot() : source;
        var enchantedId = sourceSnapshot == null ? entry.getTargetId() : sourceSnapshot.getAttachedTo();
        if (enchantedId == null) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (creature == null) {
            return;
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, sourceSnapshot));
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, creature, e.counterType(), amount);
    }
}
