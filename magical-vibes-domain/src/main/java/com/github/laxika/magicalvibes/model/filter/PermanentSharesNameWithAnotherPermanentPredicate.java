package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent when at least one other permanent on any battlefield has the same name.
 * Requires {@code gameData} on the {@link FilterContext}. Used by Eye of Singularity's ETB wipe.
 */
public record PermanentSharesNameWithAnotherPermanentPredicate() implements PermanentPredicate {
}
