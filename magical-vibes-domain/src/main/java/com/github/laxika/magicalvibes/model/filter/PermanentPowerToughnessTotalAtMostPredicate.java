package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power plus toughness is at most {@code maxTotal}.
 */
public record PermanentPowerToughnessTotalAtMostPredicate(int maxTotal) implements PermanentPredicate {
}
