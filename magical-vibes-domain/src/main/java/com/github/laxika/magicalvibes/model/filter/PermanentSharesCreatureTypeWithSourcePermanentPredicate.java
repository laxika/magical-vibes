package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share a creature type with the permanent carrying the source ability.
 * Changeling and effective creature types are respected by the engine evaluator.
 */
public record PermanentSharesCreatureTypeWithSourcePermanentPredicate() implements PermanentPredicate {
}
