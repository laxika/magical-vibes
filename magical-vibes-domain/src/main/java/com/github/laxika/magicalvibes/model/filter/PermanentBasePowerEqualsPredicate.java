package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current base power equals the supplied value. */
public record PermanentBasePowerEqualsPredicate(int basePower) implements PermanentPredicate {
}
