package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * The legal answer space of a {@link PendingInteraction}, exposed uniformly via
 * {@link PendingInteraction#legalOptions()} so generic consumers (the AI simulator's move
 * enumeration and rollout fallback, and eventually a generic frontend renderer) can
 * enumerate candidate answers without per-kind knowledge.
 *
 * <p>Each shape corresponds 1:1 to one {@code InteractionAnswer} wire-payload shape, so a
 * consumer can construct a concrete legal answer from the shape alone:
 * {@link CardIndexPick} → {@code CardIndexChosen}, {@link GraveyardIndexPick} →
 * {@code GraveyardCardChosen}, {@link LibraryIndexPick} → {@code LibraryCardChosen},
 * {@link PermanentPick} → {@code PermanentChosen}, {@link MultiCardPick} → {@code CardsChosen},
 * {@link MultiPermanentPick} → {@code PermanentsChosen}, {@link ListPick} →
 * {@code ListChoiceMade}, {@link AcceptDecline} → {@code MayAbilityChosen},
 * {@link NumberPick} → {@code NumberChosen}. Combinatorial kinds whose answers are orderings
 * or assignments (scry splits, library reorders, combat declarations, damage assignment) are
 * {@link Unenumerated} — enumerating a useful subset of their answer space is policy, not
 * legality, and stays with the consumer.
 *
 * <p>These shapes are descriptive, derived views over the interaction record's own
 * components: the answer handlers remain the validation authority (e.g. a {@code minCount}
 * of 0 does not promise that every handler accepts an empty selection in every flow).
 */
public sealed interface InteractionOptions {

    /** Shared instance for {@link AcceptDecline} (the shape carries no data). */
    InteractionOptions ACCEPT_DECLINE = new AcceptDecline();

    /** Shared instance for {@link Unenumerated} (the shape carries no data). */
    InteractionOptions UNENUMERATED = new Unenumerated();

    /**
     * Pick one card by index into the presented list (hand, revealed cards, …).
     * {@code declinable} means the answer {@code -1} declines the choice.
     */
    record CardIndexPick(List<Integer> validIndices, boolean declinable) implements InteractionOptions {
    }

    /**
     * Pick one card by index into the presented graveyard (or cross-graveyard pool).
     * {@code declinable} means the answer {@code -1} declines the choice.
     */
    record GraveyardIndexPick(List<Integer> validIndices, boolean declinable) implements InteractionOptions {
    }

    /**
     * Pick one card by index into a presented library subset of {@code cardCount} cards.
     * {@code declinable} means the answer {@code -1} fails to find.
     */
    record LibraryIndexPick(int cardCount, boolean declinable) implements InteractionOptions {
    }

    /** Pick one permanent (or player, for any-target choices) by ID. */
    record PermanentPick(List<UUID> validIds) implements InteractionOptions {
    }

    /** Pick between {@code minCount} and {@code maxCount} cards by ID. */
    record MultiCardPick(List<UUID> validCardIds, int minCount, int maxCount) implements InteractionOptions {
    }

    /** Pick between {@code minCount} and {@code maxCount} permanents by ID. */
    record MultiPermanentPick(List<UUID> validIds, int minCount, int maxCount) implements InteractionOptions {
    }

    /** Pick one value from a presented list (colors, keywords, card names, top/bottom, …). */
    record ListPick(List<String> options) implements InteractionOptions {
    }

    /** Accept or decline (a may ability). */
    record AcceptDecline() implements InteractionOptions {
    }

    /** Pick a number between {@code min} and {@code max} inclusive (X values, bids). */
    record NumberPick(int min, int max) implements InteractionOptions {
    }

    /**
     * The answer space is combinatorial (an ordering, split, or assignment) and is not
     * enumerated here; consumers keep their own policy for these kinds.
     */
    record Unenumerated() implements InteractionOptions {
    }
}
