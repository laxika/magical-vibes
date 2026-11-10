package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DayNightTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_DAY_NIGHT_CHANGE)
    private boolean handleDayNightChange(TriggerMatchContext match, TriggerContext context) {
        Card sourceCard = match.permanent().getCard();
        if (match.admits(TargetPredicate.Kind.PERMANENT)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DayNightTriggerTarget(
                    sourceCard, match.controllerId(), match.effects(), match.permanent().getId()));
        } else {
            match.gameData().enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    match.stackEffects(),
                    null,
                    match.permanent().getId()));
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when day/night changes",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
