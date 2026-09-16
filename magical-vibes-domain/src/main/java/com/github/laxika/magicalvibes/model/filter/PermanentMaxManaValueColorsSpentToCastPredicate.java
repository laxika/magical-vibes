package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is at most the number of distinct colors of mana spent to
 * cast the evaluating spell.
 */
public record PermanentMaxManaValueColorsSpentToCastPredicate() implements PermanentPredicate {
}
