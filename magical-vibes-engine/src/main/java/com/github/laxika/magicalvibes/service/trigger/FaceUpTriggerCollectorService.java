package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
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

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class,
            watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP)
    private boolean handleTurnedCreatureDealsPowerDamageToEachOpponent(TriggerMatchContext match,
                                                                         DealDamageToPlayersEffect effect,
                                                                         TriggerContext ctx) {
        if (effect.recipient() != DamageRecipient.EACH_OPPONENT
                || !(effect.amount() instanceof SourcePower)) {
            return handleCreatureTurnsFaceUp(match, effect, ctx);
        }

        TriggerContext.PermanentTurnsFaceUp faceUp = (TriggerContext.PermanentTurnsFaceUp) ctx;
        Permanent turnedPermanent = faceUp.turnedPermanent();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                turnedPermanent.getId());
        entry.setSourcePermanentSnapshot(turnedPermanent);
        entry.setDamageSourceCard(turnedPermanent.getCard());
        entry.setTriggeringPermanentId(turnedPermanent.getId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is turned face up",
                match.gameData().id, sourceCard.getName(), turnedPermanent.getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP)
    private boolean handleCreatureTurnsFaceUp(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentTurnsFaceUp faceUp = (TriggerContext.PermanentTurnsFaceUp) ctx;
        Card sourceCard = match.permanent().getCard();
        CardEffect resolvedEffect = effect;
        boolean untapsTurnedPermanent = resolvedEffect instanceof UntapPermanentsEffect untap
                && untap.scope() == TapUntapScope.TARGET;
        if (!untapsTurnedPermanent && resolvedEffect.targetSpec().declaredTarget() != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(resolvedEffect)),
                    match.permanent().getId(), faceUp.turnedPermanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers when {} is turned face up (awaiting target)",
                    match.gameData().id, sourceCard.getName(), faceUp.turnedPermanent().getCard().getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(resolvedEffect)),
                faceUp.turnedPermanent().getId(),
                match.permanent().getId());
        entry.setTriggeringPermanentId(faceUp.turnedPermanent().getId());
        // This ID records the turn-up event, not a chosen target. The trigger must still
        // resolve if the turned permanent dies before the ability resolves.
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is turned face up",
                match.gameData().id, sourceCard.getName(), faceUp.turnedPermanent().getCard().getName());
        return true;
    }
}
