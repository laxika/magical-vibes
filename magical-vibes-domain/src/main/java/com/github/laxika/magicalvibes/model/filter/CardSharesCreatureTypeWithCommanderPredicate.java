package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares a creature type with one of the evaluating player's commanders.
 * The command-zone lookup is supplied by the engine during predicate evaluation.
 */
public record CardSharesCreatureTypeWithCommanderPredicate() implements CardPredicate {
}
