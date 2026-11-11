package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers that watch a creature being turned face up. */
@Slf4j
@Service
public class FaceUpTriggerCollectorService {

    private final GameLogService gameLogService;

    public FaceUpTriggerCollectorService(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP)
    private boolean handleCreatureTurnsFaceUp(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentTurnsFaceUp faceUp = (TriggerContext.PermanentTurnsFaceUp) ctx;
        Card sourceCard = match.permanent().getCard();
        boolean untapsTurnedPermanent = match.effects().stream().anyMatch(effect ->
                effect instanceof UntapPermanentsEffect untap && untap.scope() == TapUntapScope.TARGET);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                faceUp.turnedPermanent().getId(),
                match.permanent().getId());
        entry.setTriggeringPermanentId(faceUp.turnedPermanent().getId());
        // This ID records the turn-up event, not a chosen target. The trigger must still
        // resolve if the turned permanent dies before the ability resolves.
        entry.setNonTargeting(true);
        match.gameData().handOffTriggerRecord(entry);
        if (!untapsTurnedPermanent && match.declaresTarget()) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    match.permanent().getId(), faceUp.turnedPermanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers when {} is turned face up (awaiting target)",
                    match.gameData().id, sourceCard.getName(), faceUp.turnedPermanent().getCard().getName());
            return true;
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is turned face up",
                match.gameData().id, sourceCard.getName(), faceUp.turnedPermanent().getCard().getName());
        return true;
    }
}
