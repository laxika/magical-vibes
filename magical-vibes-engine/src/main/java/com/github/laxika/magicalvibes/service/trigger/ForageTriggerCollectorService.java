package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForageTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_FORAGES)
    private boolean handleForage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        var source = match.permanent();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on forage", gameData.id, source.getCard().getName());
        return true;
    }
}
