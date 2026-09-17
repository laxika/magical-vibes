package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent whose effective power plus toughness is less than or equal to
 * {@code maxTotal}.
 */
public record PermanentPowerToughnessTotalAtMostPredicate(int maxTotal) implements PermanentPredicate {
}
