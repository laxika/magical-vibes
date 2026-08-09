package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creature cards carrying the creature subtype chosen by the source permanent.
 * Changeling cards match every creature subtype. Requires game state and a source card ID.
 */
public record CardHasSourceChosenSubtypePredicate() implements CardPredicate {
}
