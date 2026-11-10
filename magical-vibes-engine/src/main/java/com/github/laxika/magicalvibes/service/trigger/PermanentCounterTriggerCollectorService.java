package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers caused by time counters being put on a player's permanent. */
@Service
@RequiredArgsConstructor
public class PermanentCounterTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsCounterTrigger(value = CardEffect.class,
            watch = CounterWatch.ON_YOU_PUT_TIME_COUNTERS_ON_CONTROLLED_PERMANENT)
    private boolean handleTimeCountersPlaced(TriggerMatchContext match, TriggerContext context) {
        TriggerContext.TimeCountersPlaced placed = (TriggerContext.TimeCountersPlaced) context;
        if (!match.controllerId().equals(placed.placingPlayerId())) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }
}
