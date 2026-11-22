package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * What one resolution step of a {@link StepKind} did: how much, to which objects, for which players. What each kind
 * puts in each component is listed on {@link StepKind}. Immutable, so a {@link StackEntry} copy shares it.
 *
 * @param amount     how many cards, permanents, counters or tokens, or how much damage or life; never negative
 * @param objectIds  the cards or permanents the step acted on, in the order it acted
 * @param playerIds  the players the step happened to, or who controlled or owned what it acted on (CR 608.2h), each
 *                   once, in the order the step reached them
 * @param movedCards for the objects the step moved to another zone as a new object (CR 400.7), the card each became
 *                   there, so a later step of the resolution can find it (CR 400.7j); empty for a step that moved
 *                   nothing, and for an object that is a card already in the zone the step put it in (a card exiled
 *                   from a graveyard keeps its id)
 * @param lastKnownCards for the cards the step exiled from a graveyard, each as it last existed there (CR 608.2h);
 *                       empty for every other object and step
 * @param lastKnownPermanents for the permanents the step exiled, each by its id as it last existed on the battlefield
 *                       (CR 608.2h), which a reflexive or delayed ability reads too ("the exiled creature's power");
 *                       empty for every other object and step
 * @param fromHand       how many of the cards an exile step exiled came from a hand ("for each card exiled from their
 *                       hand this way"); 0 for every other step
 * @param amountByPlayer how much of the amount is each player's, for a payment or shuffle-into-library step ("the
 *                       amount of mana they paid this way"); empty for every other step
 */
public record StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds, List<MovedCard> movedCards,
                          List<LastKnownCardStats> lastKnownCards, List<LastKnownCardStats> lastKnownPermanents,
                          int fromHand, Map<UUID, Integer> amountByPlayer) {

    /** A step that did nothing. */
    public static final StepOutcome NONE = new StepOutcome(0, List.of(), List.of());

    public StepOutcome {
        if (amount < 0) {
            throw new IllegalArgumentException("A step outcome's amount is not negative: " + amount);
        }
        objectIds = List.copyOf(objectIds);
        playerIds = List.copyOf(new LinkedHashSet<>(playerIds));
        movedCards = movedCards == null ? List.of() : List.copyOf(movedCards);
        lastKnownCards = lastKnownCards == null ? List.of() : List.copyOf(lastKnownCards);
        lastKnownPermanents = lastKnownPermanents == null ? List.of() : List.copyOf(lastKnownPermanents);
        amountByPlayer = amountByPlayer == null ? Map.of() : Map.copyOf(amountByPlayer);
    }

    /** An outcome with no amount of any one player's. */
    public StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds, List<MovedCard> movedCards,
                       List<LastKnownCardStats> lastKnownCards, List<LastKnownCardStats> lastKnownPermanents,
                       int fromHand) {
        this(amount, objectIds, playerIds, movedCards, lastKnownCards, lastKnownPermanents, fromHand, Map.of());
    }

    /** An outcome that exiled no card from a hand. */
    public StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds, List<MovedCard> movedCards,
                       List<LastKnownCardStats> lastKnownCards, List<LastKnownCardStats> lastKnownPermanents) {
        this(amount, objectIds, playerIds, movedCards, lastKnownCards, lastKnownPermanents, 0);
    }

    /** An outcome that recorded no permanent as it last existed on the battlefield. */
    public StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds, List<MovedCard> movedCards,
                       List<LastKnownCardStats> lastKnownCards) {
        this(amount, objectIds, playerIds, movedCards, lastKnownCards, List.of());
    }

    /** An outcome that recorded no card as it last existed in a graveyard. */
    public StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds, List<MovedCard> movedCards) {
        this(amount, objectIds, playerIds, movedCards, List.of());
    }

    /** An outcome that moved no object to a new zone as a new card. */
    public StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds) {
        this(amount, objectIds, playerIds, List.of());
    }

    /** An outcome of {@code objectIds.size()} objects. */
    public static StepOutcome ofObjects(List<UUID> objectIds, List<UUID> playerIds) {
        return new StepOutcome(objectIds.size(), objectIds, playerIds);
    }

    /** An outcome of {@code objectIds.size()} objects, some of which became {@code movedCards} in another zone. */
    public static StepOutcome ofObjects(List<UUID> objectIds, List<UUID> playerIds, List<MovedCard> movedCards) {
        return new StepOutcome(objectIds.size(), objectIds, playerIds, movedCards);
    }

    /** An outcome that counts {@code amount} for players and acted on no object (life gained or lost). */
    public static StepOutcome ofAmount(int amount, List<UUID> playerIds) {
        return new StepOutcome(Math.max(0, amount), List.of(), playerIds);
    }

    /** An outcome counted per player (what each paid, what each shuffled away): the total, and the players with any. */
    public static StepOutcome ofAmounts(Map<UUID, Integer> amounts) {
        int total = 0;
        List<UUID> payers = new ArrayList<>();
        for (Map.Entry<UUID, Integer> payment : amounts.entrySet()) {
            if (payment.getValue() > 0) {
                total = SaturatingMath.add(total, payment.getValue());
                payers.add(payment.getKey());
            }
        }
        return new StepOutcome(total, List.of(), payers, List.of(), List.of(), List.of(), 0, amounts);
    }

    /** This outcome with {@code cards}, the cards it exiled from a graveyard as each last existed there. */
    public StepOutcome withLastKnownCards(List<LastKnownCardStats> cards) {
        return new StepOutcome(amount, objectIds, playerIds, movedCards, cards, lastKnownPermanents, fromHand,
                amountByPlayer);
    }

    /** This outcome with {@code permanents}, the permanents it exiled as each last existed on the battlefield. */
    public StepOutcome withLastKnownPermanents(List<LastKnownCardStats> permanents) {
        return new StepOutcome(amount, objectIds, playerIds, movedCards, lastKnownCards, permanents, fromHand,
                amountByPlayer);
    }

    /** This outcome with {@code count} of its cards exiled from a hand. */
    public StepOutcome withFromHand(int count) {
        return new StepOutcome(amount, objectIds, playerIds, movedCards, lastKnownCards, lastKnownPermanents, count,
                amountByPlayer);
    }

    /** {@code permanentId} as it last existed on the battlefield this step exiled it from, if the step recorded it. */
    public Optional<LastKnownCardStats> lastKnownPermanent(UUID permanentId) {
        return lastKnownPermanents.stream().filter(known -> known.cardId().equals(permanentId)).findFirst();
    }

    /** {@code cardId} as it last existed in the graveyard this step exiled it from, if the step recorded it. */
    public Optional<LastKnownCardStats> lastKnownCard(UUID cardId) {
        return lastKnownCards.stream().filter(card -> card.cardId().equals(cardId)).findFirst();
    }

    /** Whether the step did anything ("if you do", "this way"). */
    public boolean happened() {
        return amount > 0;
    }

    /**
     * This outcome with {@code more} added: amounts summed, objects, players, moved cards and last-known cards
     * appended.
     */
    public StepOutcome plus(StepOutcome more) {
        List<UUID> objects = new ArrayList<>(objectIds);
        objects.addAll(more.objectIds);
        List<UUID> players = new ArrayList<>(playerIds);
        players.addAll(more.playerIds);
        List<MovedCard> moved = new ArrayList<>(movedCards);
        moved.addAll(more.movedCards);
        List<LastKnownCardStats> known = new ArrayList<>(lastKnownCards);
        known.addAll(more.lastKnownCards);
        List<LastKnownCardStats> knownPermanents = new ArrayList<>(lastKnownPermanents);
        knownPermanents.addAll(more.lastKnownPermanents);
        Map<UUID, Integer> byPlayer = new HashMap<>(amountByPlayer);
        more.amountByPlayer.forEach((playerId, part) -> byPlayer.merge(playerId, part, SaturatingMath::add));
        return new StepOutcome(SaturatingMath.add(amount, more.amount), objects, players, moved, known,
                knownPermanents, SaturatingMath.add(fromHand, more.fromHand), byPlayer);
    }
}
