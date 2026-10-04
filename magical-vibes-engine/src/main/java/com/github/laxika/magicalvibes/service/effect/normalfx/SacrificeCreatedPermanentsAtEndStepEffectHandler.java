package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeCreatedPermanentsAtEndStepEffect} by queueing a
 * single delayed trigger that sacrifices the permanents created earlier
 * in this same resolution ({@code StackEntry.createdPermanentIds}) simultaneously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeCreatedPermanentsAtEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatedPermanentsAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCreatedPermanentIds().isEmpty()) return;
        var filter = new PermanentAnyOfPredicate(
                entry.getCreatedPermanentIds().stream()
                        .map(id -> (PermanentPredicate)
                                new PermanentIsSpecificPermanentPredicate(id))
                        .toList());
        gameData.queueDelayedAction(new DelayedEndStepTrigger(
                entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), null,
                new SacrificePermanentsEffect(
                        Integer.MAX_VALUE, filter, SacrificeRecipient.CONTROLLER).withSimultaneousChoices()));
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at end step by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
