package com.github.laxika.magicalvibes.networking.model;

/**
 * The answer-payload shape of a pending interaction, mirroring the domain's
 * {@code InteractionOptions} / the engine's {@code InteractionAnswer} shapes 1:1. The client
 * renders a prompt from the shape (plus the prompt message's presentation fields) and answers
 * with the same shape, so neither side needs per-interaction-kind wire messages.
 */
public enum InteractionShape {
    /** Pick one card by index into a presented or implicit list (own hand, revealed cards). */
    CARD_INDEX_PICK,
    /** Pick one card by index into the presented graveyard (or cross-graveyard pool). */
    GRAVEYARD_INDEX_PICK,
    /** Pick one card by index into a presented library subset. */
    LIBRARY_INDEX_PICK,
    /** Pick one permanent (or player, for any-target choices) by ID. */
    PERMANENT_PICK,
    /** Pick up to {@code maxCount} cards by ID. */
    MULTI_CARD_PICK,
    /** Pick up to {@code maxCount} permanents by ID. */
    MULTI_PERMANENT_PICK,
    /** Pick one value from a presented list of strings. */
    LIST_PICK,
    /** Accept or decline (a may ability). */
    ACCEPT_DECLINE,
    /** Pick a number between 0 and {@code maxCount} inclusive (X values, bids). */
    NUMBER_PICK,
    /** Split the presented cards into a top-of-library ordering and a bottom ordering (scry). */
    SCRY_ORDER,
    /** Produce a full ordering of the presented cards. */
    CARD_ORDER,
    /** Pick one presented card for the hand and one for the top of the library. */
    HAND_TOP_BOTTOM
}
