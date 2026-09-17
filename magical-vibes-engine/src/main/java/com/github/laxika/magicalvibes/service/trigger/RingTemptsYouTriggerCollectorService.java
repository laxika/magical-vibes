package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects abilities that trigger whenever their controller is tempted by the Ring. */
@Slf4j
@Service
@RequiredArgsConstructor
public class RingTemptsYouTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_RING_TEMPTS_YOU)
    private boolean handleRingTemptsYou(TriggerMatchContext match, CardEffect trigger, TriggerContext context) {
        if (!(context instanceof TriggerContext.RingTemptsYou)) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when its controller is tempted by the Ring",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
