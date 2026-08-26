package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers that watch a creature being turned face up. */
@Slf4j
@Service
public class FaceUpTriggerCollectorService {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    public FaceUpTriggerCollectorService(GameLogService gameLogService,
                                         PredicateEvaluationService predicateEvaluationService) {
        this.gameLogService = gameLogService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class,
            slot = EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP)
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

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP)
    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP)
    private boolean handleCreatureTurnsFaceUp(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentTurnsFaceUp faceUp = (TriggerContext.PermanentTurnsFaceUp) ctx;
        CardEffect resolvedEffect = effect;
        if (effect instanceof TriggeringCardConditionalEffect conditional
                && !predicateEvaluationService.matchesCardPredicate(
                        faceUp.turnedPermanent().getCard(), conditional.predicate(), null,
                        match.gameData(), match.controllerId())) {
            return false;
        } else if (effect instanceof TriggeringCardConditionalEffect conditional) {
            resolvedEffect = conditional.wrapped();
        }
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(resolvedEffect)),
                faceUp.turnedPermanent().getId(),
                match.permanent().getId());
        entry.setTriggeringPermanentId(faceUp.turnedPermanent().getId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is turned face up",
                match.gameData().id, sourceCard.getName(), faceUp.turnedPermanent().getCard().getName());
        return true;
    }
}
