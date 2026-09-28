package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConjureTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_CONJURES)
    private boolean handleConjure(TriggerMatchContext match, CardEffect effect, TriggerContext context) {
        var gameData = match.gameData();
        var source = match.permanent();
        var conjure = (TriggerContext.Conjure) context;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId());
        entry.setEventValue(conjure.cardCount());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on conjure", gameData.id, source.getCard().getName());
        return true;
    }
}
