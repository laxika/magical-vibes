package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power plus toughness is at least {@code minTotal}.
 */
public record PermanentPowerToughnessTotalAtLeastPredicate(int minTotal) implements PermanentPredicate {
}
