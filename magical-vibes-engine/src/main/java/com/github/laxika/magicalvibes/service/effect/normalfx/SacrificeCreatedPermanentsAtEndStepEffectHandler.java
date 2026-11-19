package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        CardEffect sacrifice = new SacrificePermanentsEffect(
                Integer.MAX_VALUE, filter, Subjects.you()).withSimultaneousChoices();
        ActivePlayer whose = ((SacrificeCreatedPermanentsAtEndStepEffect) effect).controllerEndStepOnly()
                ? ActivePlayer.YOU : ActivePlayer.ANY;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(whose), entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), null,
                sacrifice));
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at end step by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
