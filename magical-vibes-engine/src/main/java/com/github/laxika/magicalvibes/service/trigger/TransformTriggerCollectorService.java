package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
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

/** Collects non-targeting triggers that watch an ally permanent transform. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransformTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_TRANSFORMS)
    private boolean handleAllyPermanentTransform(TriggerMatchContext match, TriggerContext context) {
        TriggerContext.PermanentTransforms transforms = (TriggerContext.PermanentTransforms) context;
        Card sourceCard = match.permanent().getCard();
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        trigger.setTriggeringPermanentId(transforms.transformedPermanent().getId());
        trigger.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        match.gameData().enqueueTrigger(trigger);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when an ally permanent transforms",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
