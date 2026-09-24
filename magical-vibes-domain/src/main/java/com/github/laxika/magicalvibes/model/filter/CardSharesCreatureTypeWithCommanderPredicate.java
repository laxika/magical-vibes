package com.github.laxika.magicalvibes.model.filter;

/** Matches a creature card that shares a creature type with one of its controller's commanders. */
public record CardSharesCreatureTypeWithCommanderPredicate() implements CardPredicate {
}
