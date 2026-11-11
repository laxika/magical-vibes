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
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DungeonTriggerCollectorService {

    private final GameLogService gameLogService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_COMPLETES_DUNGEON)
    private boolean handleDungeonCompletion(TriggerMatchContext match, TriggerContext context) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !match.admits(TargetPredicate.Kind.PLAYER)
                && !match.admits(TargetPredicate.Kind.PERMANENT)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard,
                    match.controllerId(),
                    match.effects(),
                    null,
                    match.targetIsOptional() ? 0 : 1));
            triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(match.gameData());
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers on dungeon completion (awaiting graveyard target)",
                    match.gameData().id, sourceCard.getName());
            return true;
        }

        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on dungeon completion", match.gameData().id, sourceCard.getName());
        return true;
    }
}
