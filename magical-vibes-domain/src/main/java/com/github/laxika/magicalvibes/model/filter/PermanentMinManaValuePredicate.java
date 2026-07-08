package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value is at least {@code minManaValue}. Used for effects like
 * "Destroy all creatures with mana value 4 or greater" (Austere Command).
 */
public record PermanentMinManaValuePredicate(int minManaValue) implements PermanentPredicate {
}
