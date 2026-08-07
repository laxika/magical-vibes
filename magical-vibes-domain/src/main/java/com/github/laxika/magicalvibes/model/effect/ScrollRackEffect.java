package com.github.laxika.magicalvibes.model.effect;

/**
 * Scroll Rack's activated ability: the controller exiles any number of cards from their hand face
 * down, puts that many cards from the top of their library into their hand, then puts the exiled
 * cards back on top of their library in any order.
 *
 * <p>The whole swap happens inside one resolution, so no player ever sees the set-aside cards; they
 * are carried by the interaction chain rather than parked in the exile zone. Moving library cards to
 * hand here is <em>not</em> drawing — the card says "put", not "draw" — so draw triggers and draw
 * replacements do not apply; a short library simply moves fewer cards.
 *
 * <p>Begins a {@code PutCardsFromHandOnLibraryCardChoice} in its {@code swapWithLibraryTop} mode,
 * which finishes with a {@code LibraryReorder} for the "in any order" step.
 */
public record ScrollRackEffect() implements CardEffect {
}
