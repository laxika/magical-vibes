package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostRandomCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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
public class LifeGainTriggerCollectorService {

    private final GameLogService gameLogService;

    /**
     * "Whenever you gain life, you may …": the ability goes on the stack as written and asks as it resolves (CR 603.5),
     * with the life gained as the event's value ("that many", Nykthos Paragon) and a "do this only once each turn" used
     * up as it is accepted (CR 603.2h).
     */
    @CollectsLifeTrigger(value = MayEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                null, match.permanent().getId(), ((TriggerContext.LifeGain) ctx).lifeGainedAmount(),
                match.markSourceOncePerTurnOnAcceptance(), null, null);
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain and queues a may ability",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTrigger(value = PerpetuallyBoostRandomCreatureCardInHandEffect.class,
            watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainRandomCreatureBoost(TriggerMatchContext match,
                                                       PerpetuallyBoostRandomCreatureCardInHandEffect effect,
                                                       TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(((TriggerContext.LifeGain) ctx).lifeGainedAmount());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain (random creature card boost)",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever you gain life, you may pay {cost}. If you do, …": the ability goes on the stack as written and asks as it
     * resolves (CR 603.5), its target chosen as it triggers (a "you may pay" whose effect targets no player offers
     * creatures: Cradle of Vitality's "target creature"), with the life gained as the event's value ("a +1/+1 counter
     * for each 1 life you gained").
     */
    @CollectsLifeTrigger(value = MayPayManaEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainMayPay(TriggerMatchContext match,
                                         MayPayManaEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        int lifeGained = ((TriggerContext.LifeGain) ctx).lifeGainedAmount();
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, match.controllerId(), List.of(effect)));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers on life gain and needs a graveyard target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    List.of(effect),
                    match.permanent().getId(),
                    !effect.targetSpec().admits(TargetPredicate.Kind.PLAYER),
                    lifeGained));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers on life gain and needs a target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(lifeGained);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTriggers({
            @CollectsLifeTrigger(value = SequenceEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE),
            @CollectsLifeTrigger(value = SequenceEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    })
    private boolean handleLifeGainSequence(TriggerMatchContext match,
                                            SequenceEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    List.of(effect),
                    match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} sequence trigger needs a target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain (sequence)", match.gameData().id, sourceCard.getName());
        return true;
    }
}
