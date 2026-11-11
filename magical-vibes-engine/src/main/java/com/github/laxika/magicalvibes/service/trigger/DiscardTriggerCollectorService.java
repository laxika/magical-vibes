package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger collectors for discard and cycling events. Every watch has one default, which records the discard on the
 * ability's entry whatever its effects are ({@link #recordDiscard}); a targeted ability has its target chosen as it goes
 * on the stack (CR 603.3d) by a choice carrying the same record.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscardTriggerCollectorService {

    private final GameLogService gameLogService;

    /**
     * Puts a "whenever you cycle a card" ability on the stack, whole, for a cycling the collector judged
     * ({@code LibraryTrigger.LibraryEvent.CYCLES}), choosing its target first if it has one.
     */
    public boolean collectCycling(TriggerMatchContext match, TriggerContext ctx) {
        return collect(match, ctx, "cycling");
    }

    /**
     * Puts a "whenever a spell or ability an opponent controls causes you to discard a card" ability on the stack,
     * whole, for a discard the collector judged ({@code LibraryTrigger.LibraryEvent.DISCARDS_BY_OPPONENT}).
     */
    public boolean collectOpponentCausedDiscard(TriggerMatchContext match, TriggerContext ctx) {
        return collect(match, ctx, "opponent-caused discard");
    }

    /**
     * "Whenever you discard a card, …" and "whenever an opponent discards a card, …": the ability goes on the stack as
     * written, one object of its effects (CR 603.3); a modal one announces its mode there (CR 603.3c), among those its
     * restriction leaves ("choose one that hasn't been chosen this turn": Monument to Endurance); a permanent or player
     * target is chosen as it goes on the stack (CR 603.3d). The entry records the discard ({@link #recordDiscard}). An
     * ability with an effect that targets something else (a card in a graveyard) is collected by no handler and doesn't
     * trigger.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_OPPONENT_DISCARDS)
    private boolean handleDiscardDefault(TriggerMatchContext match, TriggerContext ctx) {
        if (targetsOtherThanPermanentOrPlayer(match)) {
            return false;
        }
        return collect(match, ctx, "discard");
    }

    /**
     * "Whenever a player cycles a card, …" (Lightning Rift, Astral Slide, Stoic Champion, Fleeting Aven), as
     * {@link #handleDiscardDefault}: cycling discards the card (CR 702.29a), so the entry records the cycling player and
     * the cycled card as a discard's entry records the discard.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_ANY_PLAYER_CYCLES)
    private boolean handleCycleDefault(TriggerMatchContext match, TriggerContext ctx) {
        if (targetsOtherThanPermanentOrPlayer(match)) {
            return false;
        }
        return collect(match, ctx, "cycling");
    }

    /**
     * "Whenever you discard one or more cards, …": the ability goes on the stack as written, once for the discard event
     * (CR 603.2c), recording it ({@link #recordDiscard}); a targeted one has its target chosen as it goes on the stack
     * (CR 603.3d), with the number of cards discarded as the choice's event value.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleDiscardEventDefault(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.DiscardEvent)) {
            return false;
        }
        return collect(match, ctx, "discard event");
    }

    private boolean collect(TriggerMatchContext match, TriggerContext ctx, String event) {
        GameData gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        Discarded discarded = discarded(gameData, ctx);
        StackEntry entry = recordDiscard(gameData, new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()), ctx);
        gameData.handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT) || match.admits(TargetPredicate.Kind.PLAYER)) {
            var choice = new PermanentChoiceContext.DiscardControllerTriggerTarget(sourceCard, match.controllerId(),
                    match.stackEffects(), match.permanent().getId(), discarded.count());
            gameData.queueInteraction(discarded.playerId() == null ? choice
                    : choice.withDiscard(discarded.playerId(), discarded.cards(), discarded.stays()));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on {}", gameData.id, sourceCard.getName(), event);
        return true;
    }

    /** Whether an effect of the ability declares a target that is neither a permanent nor a player. */
    private static boolean targetsOtherThanPermanentOrPlayer(TriggerMatchContext match) {
        return match.effects().stream().anyMatch(effect -> effect.targetSpec().declaredTarget() != null
                && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                && !effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
    }

    /**
     * The discard of {@code ctx}: the player who discarded (or cycled), the cards discarded with the graveyard stay each
     * went to, and the number of cards of a discard event (0 for one card); no player for an event of another kind.
     */
    private record Discarded(UUID playerId, List<Card> cards, Map<UUID, Long> stays, int count) {
    }

    private static Discarded discarded(GameData gameData, TriggerContext ctx) {
        UUID playerId;
        List<Card> cards;
        int count = 0;
        switch (ctx) {
            case TriggerContext.Discard discard -> {
                playerId = discard.discardingPlayerId();
                cards = discard.discardedCard() == null ? List.of() : List.of(discard.discardedCard());
            }
            case TriggerContext.Cycle cycle -> {
                playerId = cycle.cyclingPlayerId();
                cards = cycle.cycledCard() == null ? List.of() : List.of(cycle.cycledCard());
            }
            case TriggerContext.DiscardEvent discardEvent -> {
                playerId = discardEvent.discardingPlayerId();
                cards = discardEvent.discardedCards();
                count = discardEvent.discardedCount();
            }
            default -> {
                return new Discarded(null, List.of(), Map.of(), 0);
            }
        }
        Map<UUID, Long> stays = new LinkedHashMap<>();
        for (Card card : cards) {
            stays.put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        return new Discarded(playerId, cards, stays, count);
    }

    /**
     * Records the discard on {@code entry}, whatever its effects are: the discarding (or cycling, CR 702.29a) player as
     * its triggering player and, for an ability that chose no target, as its non-targeting target ("that player"); the
     * cards discarded as its triggering cards, each with the graveyard stay it went to ("those cards", "them": a card
     * that left the graveyard and returned is a new object, CR 400.7), a lone card also as its triggering card ("that
     * card", "it"); and for "one or more cards" their number as its event value. An event of another kind records
     * nothing.
     */
    static StackEntry recordDiscard(GameData gameData, StackEntry entry, TriggerContext ctx) {
        Discarded discarded = discarded(gameData, ctx);
        if (discarded.playerId() == null) {
            return entry;
        }
        if (ctx instanceof TriggerContext.DiscardEvent) {
            entry.setEventValue(discarded.count());
        }
        return recordDiscard(entry, discarded.playerId(), discarded.cards(), discarded.stays(), true);
    }

    /**
     * Records a discard carried by a target choice ({@code DiscardControllerTriggerTarget.withDiscard}) on the entry the
     * choice makes, as {@link #recordDiscard(GameData, StackEntry, TriggerContext)} records it: the player as triggering
     * player (and, when {@code thatPlayerAsTarget} and the entry has no target, as its non-targeting "that player"), the
     * cards as triggering cards with their stays, a lone card also as the triggering card. A targeted ability that chose
     * no target (an optional one declined) gets no "that player" target.
     */
    public static StackEntry recordDiscard(StackEntry entry, UUID playerId, List<Card> cards, Map<UUID, Long> stays,
                                           boolean thatPlayerAsTarget) {
        if (playerId == null) {
            return entry;
        }
        entry.setTriggeringPlayerId(playerId);
        if (thatPlayerAsTarget && entry.getTargetId() == null) {
            entry.setTargetId(playerId);
            entry.setNonTargeting(true);
        }
        entry.setTriggeringCards(cards);
        entry.getEventCardGraveyardEntryVersions().putAll(stays);
        if (cards.size() == 1) {
            Card card = cards.getFirst();
            entry.setTriggeringCard(card.getId(), stays.getOrDefault(card.getId(), 0L));
        }
        return entry;
    }
}
