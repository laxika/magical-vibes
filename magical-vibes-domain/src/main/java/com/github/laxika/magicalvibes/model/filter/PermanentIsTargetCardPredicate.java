package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the permanent whose card is the card identified by the target card in the filter context.
 * Used by effects that refer to "another" permanent than the one an earlier part of the same
 * ability targeted (Feast of Worms: "its controller sacrifices another land").
 */
public record PermanentIsTargetCardPredicate() implements PermanentPredicate {
}
