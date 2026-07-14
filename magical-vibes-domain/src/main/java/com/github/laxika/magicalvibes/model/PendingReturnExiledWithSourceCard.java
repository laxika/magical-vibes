package com.github.laxika.magicalvibes.model;

/**
 * Marks that the active {@code LIBRARY_REVEAL_CHOICE} is the controller choosing one card exiled
 * "with" a source permanent to return from exile to their hand (Endless Horizons upkeep). Only the
 * selected card leaves exile; the rest stay exiled.
 */
public record PendingReturnExiledWithSourceCard() implements PendingInteraction {
}
