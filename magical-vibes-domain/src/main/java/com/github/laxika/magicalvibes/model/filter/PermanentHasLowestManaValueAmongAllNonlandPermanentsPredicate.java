package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a nonland permanent if it has the lowest mana value among all nonland permanents on the
 * battlefield. Multiple permanents can match if tied for the lowest mana value. Requires game data
 * to evaluate.
 */
public record PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate() implements PermanentPredicate {
}
