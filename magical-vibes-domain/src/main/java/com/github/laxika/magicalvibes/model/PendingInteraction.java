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
        PendingInteraction.HandTopBottomChoice, PendingInteraction.LibraryReorder,
        PendingInteraction.MayAbilityChoice, PendingInteraction.KnowledgePoolCastChoice,
        PendingInteraction.MirrorOfFateChoice {

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

    /**
     * Accept/decline prompt for the head of {@link GameData#pendingMayAbilities}.
     * {@code description} and {@code manaCost} mirror that head entry; whether the player
     * can currently pay {@code manaCost} is computed at prompt time from their mana pool.
     */
    record MayAbilityChoice(UUID playerId, String description, String manaCost)
            implements PendingInteraction {
    }

    /**
     * Knowledge Pool: the caster may cast one of the pool's other nonland exiled cards without
     * paying its cost (or decline with an empty selection). {@code validCardIds} keeps the
     * begin-time order; the card views are re-derived from the pool at prompt time (the pool
     * permanent is found via the queued {@link PendingKnowledgePoolCast}).
     */
    record KnowledgePoolCastChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {
    }

    /**
     * Mirror of Fate: choose up to seven face-up exiled cards to put on top of the library.
     * {@code validCardIds} keeps the begin-time order; views are re-derived from the player's
     * exile zone at prompt time.
     */
    record MirrorOfFateChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {
    }
}
