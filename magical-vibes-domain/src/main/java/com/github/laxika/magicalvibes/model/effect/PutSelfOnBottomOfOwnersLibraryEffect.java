package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect: after resolution, the spell is put on the bottom of its owner's library
 * instead of going to the graveyard.
 */
public record PutSelfOnBottomOfOwnersLibraryEffect() implements CardEffect {
}
