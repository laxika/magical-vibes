package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares a creature type with the source permanent.
 * Changeling, transient creature-type changes, and other effective creature types are respected.
 * Needs game data and the source card ID to evaluate.
 */
public record CardSharesCreatureTypeWithSourcePredicate() implements CardPredicate {
}
