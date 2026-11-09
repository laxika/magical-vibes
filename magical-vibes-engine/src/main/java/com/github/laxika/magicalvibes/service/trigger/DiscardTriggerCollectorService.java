package com.github.laxika.magicalvibes.service.trigger;


import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.GameData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.github.laxika.magicalvibes.model.GameLog;
/**
 * Trigger collectors for discard and cycling events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscardTriggerCollectorService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;

    /**
     * Puts the effect of a "whenever you cycle a card" ability on the stack for a cycling the collector judged
     * ({@code LibraryTrigger.LibraryEvent.CYCLES}), choosing its target first if it has one.
     */
    public boolean collectCycling(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    match.permanent().getId()));
        } else {
            gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()), ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", gameData.id, sourceCard.getName());
        return true;
    }

    /**
     * Puts the effect of a "whenever a spell or ability an opponent controls causes you to discard a card" ability on
     * the stack for a discard the collector judged ({@code LibraryTrigger.LibraryEvent.DISCARDS_BY_OPPONENT}).
     */
    public boolean collectOpponentCausedDiscard(TriggerMatchContext match, CardEffect effect,
                                                TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()), ctx));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on opponent-caused discard", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = MayEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    private boolean handleCycleMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(may)), match.permanent().getId()));
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                null,
                match.permanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = ReturnToHandEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    private boolean handleCycleReturnToHand(TriggerMatchContext match, ReturnToHandEffect effect,
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
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = PutCountersOnSelfEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    private boolean handleCyclePutCountersOnSelf(TriggerMatchContext match,
                                                  PutCountersOnSelfEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever you discard a card, …" and "whenever an opponent discards a card, …", for an effect that chooses no target
     * and no handler of its own collects: the ability goes on the stack as written, one object of its effect (CR 603.3);
     * a modal one announces its mode there (CR 603.3c), among those its restriction leaves ("choose one that hasn't been
     * chosen this turn": Monument to Endurance). The entry records the discard ({@link #recordDiscard}). A targeted
     * effect needs a handler that chooses its target as the ability goes on the stack (CR 603.3d).
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_OPPONENT_DISCARDS)
    private boolean handleDiscardDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        if (effect.targetSpec().declaredTarget() != null) {
            return false;
        }
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        gameData.enqueueTrigger(recordDiscard(gameData, entry, ctx));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard ({})", gameData.id, sourceCard.getName(),
                effect.getClass().getSimpleName());
        return true;
    }

    @CollectsLibraryTrigger(value = BoostSelfEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    @CollectsLibraryTrigger(value = BoostSelfEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    private boolean handleSelfBoostOnDiscard(TriggerMatchContext match, BoostSelfEffect trigger, TriggerContext ctx) {
        // Cycling discards the card, so this controller-discard trigger handles both cycling and discard.
        // Queue it as a proper triggered ability carrying the source permanent id so the self-boost lands on it.
        // (Hekma Sentinels)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()), ctx));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (self-boost)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = SequenceEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    @CollectsLibraryTrigger(value = SequenceEffect.class, watch = LibraryWatch.ON_OPPONENT_DISCARDS)
    private boolean handleSequenceOnDiscard(TriggerMatchContext match, SequenceEffect trigger, TriggerContext ctx) {
        // Multi-step discard triggers must stay ONE atomic triggered ability (SequenceEffect), so queue
        // a single stack entry carrying the source permanent id. Each self step then resolves against
        // this creature.
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (trigger.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || trigger.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)),
                    match.permanent().getId()));
        } else {
            gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    match.permanent().getId()), ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (sequence)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = GrantKeywordEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    @CollectsLibraryTrigger(value = GrantKeywordEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    private boolean handleGrantKeywordOnDiscard(TriggerMatchContext match, GrantKeywordEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, target creature gains [keyword] until end of turn."
        // Cycling discards the card (CR 702.29e), so this single controller-discard trigger fires for
        // both. (Zenith Seeker)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (trigger.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            // Targeted grant: queue a target choice so the controller picks the creature before the
            // ability goes on the stack (resolves above the cycling draw). Serviced by
            // TriggeredAbilityQueueService.processNextDiscardControllerTriggerTarget.
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        } else {
            // Non-targeting grant (self / your creatures) — straight onto the stack as a triggered ability.
            gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    match.permanent().getId()), ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (grant keyword)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = BoostTargetCreatureEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    private boolean handleBoostTargetCreatureOnDiscard(TriggerMatchContext match, BoostTargetCreatureEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, target creature an opponent controls gets -X/-Y until
        // end of turn." Cycling discards the card (CR 702.29e), so this single controller-discard
        // trigger fires for both. The effect's targetSpec predicate (a "creature an opponent controls"
        // filter) drives the legal-target list via the DiscardControllerTriggerTarget pipeline, so the
        // controller picks the creature before the ability goes on the stack. (Ominous Sphinx)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (boost target creature)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = SourceFightsTargetCreatureEffect.class,
            watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    private boolean handleSourceFightOnDiscard(TriggerMatchContext match,
            SourceFightsTargetCreatureEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard (fight target creature)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTrigger(value = PutCounterOnTargetPermanentEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    private boolean handlePutCounterOnTargetOnDiscard(TriggerMatchContext match,
            PutCounterOnTargetPermanentEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller discard (put counter on target)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLibraryTriggers({
            @CollectsLibraryTrigger(value = MayPayManaEffect.class, watch = LibraryWatch.ON_OPPONENT_DISCARDS),
            @CollectsLibraryTrigger(value = MayPayManaEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS),
            @CollectsLibraryTrigger(value = MayPayManaEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    })
    private boolean handleMayPayManaOnDiscard(TriggerMatchContext match, MayPayManaEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        if (trigger.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || trigger.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        } else {
            gameData.enqueueTrigger(recordDiscard(gameData, entry, ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard/cycle (may pay {})",
                gameData.id, sourceCard.getName(), trigger.manaCost());
        return true;
    }

    /**
     * "Whenever you discard one or more cards, …": the ability goes on the stack as written, once for the discard event
     * (CR 603.2c), recording it ({@link #recordDiscard}); a targeted one has its target chosen as it goes on the stack
     * (CR 603.3d), with the number of cards discarded as the choice's event value.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleDiscardEventDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.DiscardEvent discardEvent)) {
            return false;
        }
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId(),
                    discardEvent.discardedCount()));
        } else {
            gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()), ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on a discard event ({})", gameData.id, sourceCard.getName(),
                effect.getClass().getSimpleName());
        return true;
    }

    /**
     * Records the discard on {@code entry}, whatever its effects are: the discarding player as its triggering player
     * and, for an ability that chose no target, as its non-targeting target ("that player"); the cards discarded as
     * its triggering cards, each with the graveyard stay it went to ("those cards", "them": a card that left the
     * graveyard and returned is a new object, CR 400.7), a lone card also as its triggering card ("that card", "it");
     * and for "one or more cards" their number as its event value. An event of another kind records nothing.
     */
    static StackEntry recordDiscard(GameData gameData, StackEntry entry, TriggerContext ctx) {
        UUID discardingPlayerId;
        List<Card> discardedCards;
        if (ctx instanceof TriggerContext.Discard discard) {
            discardingPlayerId = discard.discardingPlayerId();
            discardedCards = discard.discardedCard() == null ? List.of() : List.of(discard.discardedCard());
        } else if (ctx instanceof TriggerContext.DiscardEvent discardEvent) {
            discardingPlayerId = discardEvent.discardingPlayerId();
            discardedCards = discardEvent.discardedCards();
            entry.setEventValue(discardEvent.discardedCount());
        } else {
            return entry;
        }
        entry.setTriggeringPlayerId(discardingPlayerId);
        if (entry.getTargetId() == null && discardingPlayerId != null) {
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
        }
        entry.setTriggeringCards(discardedCards);
        for (Card card : discardedCards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        if (discardedCards.size() == 1) {
            Card card = discardedCards.getFirst();
            entry.setTriggeringCard(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        return entry;
    }
}
