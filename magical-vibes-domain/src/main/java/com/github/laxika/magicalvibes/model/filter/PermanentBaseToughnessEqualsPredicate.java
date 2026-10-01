package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current base toughness equals the supplied value. */
public record PermanentBaseToughnessEqualsPredicate(int baseToughness) implements PermanentPredicate {
}
