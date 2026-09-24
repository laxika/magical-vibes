package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature card that shares a creature type with one of its controller's commanders.
 * Evaluation uses the controller's command zone and honours Changeling and effective
 * all-zone creature-type grants.
 */
public record CardSharesCreatureTypeWithCommanderPredicate() implements CardPredicate {
}
