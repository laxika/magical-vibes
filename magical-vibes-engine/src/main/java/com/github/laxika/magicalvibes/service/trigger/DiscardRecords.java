package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The record of a discard or cycling event (CR 702.29a: cycling discards the card) on a triggered ability's entry,
 * whatever its effects are ({@link #record}); a target choice that carries a discard stamps it the same way.
 */
public final class DiscardRecords {

    private DiscardRecords() {
    }

    /**
     * The record of the discard {@code ctx}, stamped on the entry whatever its effects are: the discarding (or cycling, CR 702.29a) player as
     * its triggering player and, for an ability that chose no target, as its non-targeting target ("that player"); the
     * cards discarded as its triggering cards, each with the graveyard stay it went to ("those cards", "them": a card
     * that left the graveyard and returned is a new object, CR 400.7), a lone card also as its triggering card ("that
     * card", "it"); and for "one or more cards" their number as its event value. An event of another kind records
     * nothing.
     */
    public static TriggerRecord record(GameData gameData, TriggerContext ctx) {
        Discarded discarded = discarded(gameData, ctx);
        return entry -> {
            if (discarded.playerId() == null) {
                return;
            }
            if (ctx instanceof TriggerContext.DiscardEvent) {
                entry.setEventValue(discarded.count());
            }
            recordDiscard(entry, discarded.playerId(), discarded.cards(), discarded.stays(), true);
        };
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
     * Records a discard carried by a target choice ({@code DiscardControllerTriggerTarget.withDiscard}) on the entry the
     * choice makes, as {@link #record} records it: the player as triggering
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
