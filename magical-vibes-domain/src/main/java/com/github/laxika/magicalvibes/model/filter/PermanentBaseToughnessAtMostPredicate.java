package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current base toughness is at most the supplied value. */
public record PermanentBaseToughnessAtMostPredicate(int maxToughness) implements PermanentPredicate {
}
