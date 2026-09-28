package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current base power and toughness equal the supplied values. */
public record PermanentBasePowerToughnessPredicate(int power, int toughness)
        implements PermanentPredicate {
}
