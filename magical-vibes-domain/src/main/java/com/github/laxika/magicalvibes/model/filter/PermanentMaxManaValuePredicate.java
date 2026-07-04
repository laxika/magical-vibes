package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value is at most {@code maxManaValue}. Used for effects like
 * "Destroy target nonland permanent with mana value 2 or less" (Witherbloom Charm).
 */
public record PermanentMaxManaValuePredicate(int maxManaValue) implements PermanentPredicate {
}
