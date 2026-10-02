package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent-controlled creature if it has the least effective toughness among all
 * creatures controlled by opponents of the source controller. Multiple creatures can match if
 * tied for least toughness.
 */
public record PermanentHasLeastToughnessAmongOpponentCreaturesPredicate() implements PermanentPredicate {
}
