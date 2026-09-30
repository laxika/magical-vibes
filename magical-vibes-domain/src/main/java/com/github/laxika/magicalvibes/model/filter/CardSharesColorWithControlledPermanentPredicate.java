package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares at least one effective color with a permanent controlled by the
 * card's owner/controller.
 */
public record CardSharesColorWithControlledPermanentPredicate() implements CardPredicate {
}
