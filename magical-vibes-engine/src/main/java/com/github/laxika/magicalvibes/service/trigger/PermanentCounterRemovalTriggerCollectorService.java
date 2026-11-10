package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the triggers of a permanent's own +1/+1 or time counters being removed; "whenever one or more counters are
 * removed from a permanent you control" is {@link MiscTriggerCollectorService}'s.
 */
@Service
@RequiredArgsConstructor
public class PermanentCounterRemovalTriggerCollectorService {

    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTER_REMOVED)
    private boolean handleSelfPlusOnePlusOneCounterRemoved(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.PlusOnePlusOneCountersRemoved removed)
                || removed.amount() <= 0
                || match.permanent() == null
                || !match.permanent().getId().equals(removed.permanent().getId())) {
            return false;
        }

        GameData gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < removed.amount(); i++) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    match.stackEffects(),
                    null,
                    match.permanent().getId()));
        }
        return true;
    }

    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_SELF_TIME_COUNTERS_REMOVED)
    private boolean handleSelfTimeCountersRemoved(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.TimeCountersRemoved removed)
                || removed.amount() <= 0
                || match.permanent() == null
                || !match.permanent().getId().equals(removed.permanent().getId())) {
            return false;
        }

        GameData gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()));
        return true;
    }
}
