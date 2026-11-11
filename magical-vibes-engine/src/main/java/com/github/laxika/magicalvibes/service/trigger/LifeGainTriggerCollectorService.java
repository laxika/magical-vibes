package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The collectors of the life gain, loss and payment abilities ({@link LifeWatch}): "whenever you gain life", "whenever
 * an opponent gains life", "whenever you lose life", "whenever an opponent loses life" and "whenever you pay life".
 * <p>
 * Each watch has one default, and every ability it reads records the event whatever its effects are: the life gained,
 * lost or paid as the event's value ("that much", "that many"), the player it happened to as the triggering player
 * (and, for an ability that chooses no target, its non-targeting "that player", which the {@code TRIGGERING_PLAYER}
 * recipients read), the source as it was when the ability triggered, and the ability's "do this only once each turn"
 * marked as its action is taken (CR 603.2h). The effects go on the stack as written; a "you may" is asked as the
 * ability resolves (CR 603.5). The ability is one object (CR 603.3): a target is chosen as it goes on the stack
 * (CR 603.3d), by what its effects' targets admit, and the choice carries every effect and the same record into the
 * entry. The collectors in {@code TriggerCollectionService} judged the ability's event condition and intervening "if"
 * as it triggered.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifeGainTriggerCollectorService {

    private final GameLogService gameLogService;

    /**
     * The default of every life watch: the ability goes on the stack, or to its target choice, with the life event
     * recorded (see the type's documentation).
     */
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_OPPONENT_GAINS_LIFE)
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_CONTROLLER_PAYS_LIFE)
    private boolean handleLifeDefault(TriggerMatchContext match, TriggerContext ctx) {
        LifeEvent event = LifeEvent.of(ctx);
        Card sourceCard = match.permanent().getCard();
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            queueGraveyardTarget(match, 0, 0);
            return true;
        }
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            // "Up to X target creatures, where X is the life gained" (Heroic Feast): the targets are chosen group by
            // group, with the life gained as the count.
            if (match.admits(TargetPredicate.Kind.PERMANENT)
                    && sourceCard.getTargetGroups().stream().anyMatch(group -> group.getDynamicMaxTargets() != null)) {
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        sourceCard, match.controllerId(), match.effects(), match.permanent().getId(),
                        List.of(), 0, 0, List.of(), 0, List.of(), false, null, null, event.amount()));
            } else {
                match.gameData().queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                        sourceCard, match.controllerId(), match.effects(), match.permanent().getId(),
                        !match.admits(TargetPredicate.Kind.PLAYER), event.amount(),
                        event.playerId()));
            }
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers on a life event and needs a target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        if (!match.declaresTarget()) {
            entry.setTargetId(event.playerId());
            entry.setNonTargeting(true);
        }
        entry.setEventValue(event.amount());
        entry.setTriggeringPlayerId(event.playerId());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on a life event ({} life)",
                match.gameData().id, sourceCard.getName(), event.amount());
        return true;
    }

    /**
     * "Whenever one or more opponents lose life, you may cast target instant or sorcery card from your graveyard"
     * (Emet-Selch of the Third Seat): the default's graveyard target choice, needing one target (with none the ability
     * isn't put on the stack, CR 603.3d); the record is the default's.
     */
    @CollectsLifeTrigger(value = CastTargetInstantOrSorceryFromGraveyardEffect.class,
            watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    private boolean handleOpponentLifeLossCastFromGraveyard(TriggerMatchContext match,
            CastTargetInstantOrSorceryFromGraveyardEffect effect, TriggerContext ctx) {
        queueGraveyardTarget(match, 1, 1);
        return true;
    }

    private void queueGraveyardTarget(TriggerMatchContext match, int minCount, int maxCount) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                sourceCard, match.controllerId(), match.effects(), null, minCount, 0, maxCount, null, false, null,
                match.permanent().getId(), match.onceEachTurnActionKey(), List.of()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on a life event and needs a graveyard target",
                match.gameData().id, sourceCard.getName());
    }

    /** The player a life gain, loss or payment happened to, and how much. */
    private record LifeEvent(UUID playerId, int amount) {

        static LifeEvent of(TriggerContext ctx) {
            return switch (ctx) {
                case TriggerContext.LifeGain gain -> new LifeEvent(gain.gainingPlayerId(), gain.lifeGainedAmount());
                case TriggerContext.LifeLoss loss -> new LifeEvent(loss.losingPlayerId(), loss.lifeLostAmount());
                case TriggerContext.LifePayment payment ->
                        new LifeEvent(payment.payingPlayerId(), payment.lifePaidAmount());
                default -> throw new IllegalArgumentException("Not a life gain, loss or payment: " + ctx);
            };
        }
    }
}
