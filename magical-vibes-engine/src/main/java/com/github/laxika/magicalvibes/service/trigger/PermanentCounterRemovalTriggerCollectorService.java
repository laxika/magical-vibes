package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers caused by counters being removed from a permanent the watcher controls. */
@Service
@RequiredArgsConstructor
public class PermanentCounterRemovalTriggerCollectorService {
    private final ConditionEvaluationService conditionEvaluationService;

    @CollectsTrigger(value = SeekEffect.class, slot = EffectSlot.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT)
    private boolean handleCountersRemoved(TriggerMatchContext match, SeekEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CountersRemovedFromPermanent removed)
                || removed.amount() <= 0 || match.permanent() == null) {
            return false;
        }

        GameData gameData = match.gameData();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_TIME_COUNTERS_REMOVED)
    private boolean handleSelfTimeCountersRemoved(TriggerMatchContext match, CardEffect effect,
                                                  TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.TimeCountersRemoved removed)
                || removed.amount() <= 0
                || match.permanent() == null
                || !match.permanent().getId().equals(removed.permanent().getId())) {
            return false;
        }

        if (effect instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(match.gameData(), conditional.condition(),
                    ConditionContext.forPermanent(
                            match.permanent(), match.controllerId()))) {
                return false;
            }
            effect = conditional.wrapped();
        }

        GameData gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        return true;
    }
}
