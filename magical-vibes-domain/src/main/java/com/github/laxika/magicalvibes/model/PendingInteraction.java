package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A queued player decision: everything the engine needs to prompt the deciding player and
 * apply their answer later. Instances wait in {@link GameData#pendingInteractions} until
 * serviced; consumers scan the queue for the first entry of the kind they handle (see the
 * type-filtered helpers on {@link GameData}), which preserves FIFO order per kind.
 *
 * <p>This is the unification point for the legacy pending-choice subsystem: the
 * {@link PermanentChoiceContext} records are the first members, and the remaining
 * {@code Pending*} / {@code ChoiceContext} shapes migrate here incrementally
 * (see {@code REFACTOR-NOTES.md} at the repository root).
 */
public sealed interface PendingInteraction permits PermanentChoiceContext,
        PendingSphinxAmbassadorChoice, PendingCapriciousEfreetState,
        PendingKarnScionRevealChoice, PendingKarnScionExileReturn,
        PendingKarnRestart, PendingKnowledgePoolCast,
        PendingInteraction.XValueChoice, PendingInteraction.Scry,
        PendingInteraction.HandTopBottomChoice, PendingInteraction.LibraryReorder {

    // ------------------------------------------------------------------
    // Generic interaction kinds, migrated one at a time from the legacy
    // AwaitingInput / InteractionContext machinery. Each record carries
    // everything needed to prompt the deciding player and apply the answer
    // (dispatched via the engine's InteractionHandlerRegistry).
    // ------------------------------------------------------------------

    /** "Choose a value for X" (e.g. Vigil for the Lost's ETB payment, Jaya's rummage count). */
    record XValueChoice(UUID playerId, int maxValue, String prompt, String cardName)
            implements PendingInteraction {
    }

    /** Scry N: {@code cards} are held out of the library while the player splits them top/bottom. */
    record Scry(UUID playerId, java.util.List<Card> cards) implements PendingInteraction {
    }

    /** "Look at the top N cards: one to hand, one on top, rest on the bottom" (e.g. Anticipate-style picks). */
    record HandTopBottomChoice(UUID playerId, java.util.List<Card> cards) implements PendingInteraction {
    }

    /**
     * Put the given cards on the top (or bottom) of {@code deckOwnerId}'s library in an order
     * of the deciding player's choosing. {@code prompt} is the exact text shown at begin time
     * (also re-sent on reconnect).
     */
    record LibraryReorder(UUID playerId, java.util.List<Card> cards, boolean toBottom,
                          UUID deckOwnerId, String prompt) implements PendingInteraction {
    }
}
