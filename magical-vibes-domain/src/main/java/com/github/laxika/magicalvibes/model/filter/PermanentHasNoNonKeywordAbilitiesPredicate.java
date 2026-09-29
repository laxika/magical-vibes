package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose effective abilities, other than keywords, are empty. */
public record PermanentHasNoNonKeywordAbilitiesPredicate() implements PermanentPredicate {
}
