package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A card a player picked for a "discard N cards" choice, held out of their hand until the last pick so that every
 * picked card is discarded together. {@code applyDiscardToLibraryReplacement} is the player's answer, given as they
 * picked it, to a replacement that puts a discarded card on top of their library instead (Library of Leng).
 */
public record StagedDiscard(UUID playerId, Card card, boolean applyDiscardToLibraryReplacement) {
}
