package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current base power is at most the supplied value. */
public record PermanentBasePowerAtMostPredicate(int maxPower) implements PermanentPredicate {
}
