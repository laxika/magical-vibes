package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares a creature type with a creature card in its controller's library.
 * Evaluation honors Changeling and effective all-zone creature-type grants.
 */
public record CardSharesCreatureTypeWithLibraryCreaturePredicate() implements CardPredicate {
}
