package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents that currently have at least one activated ability that is not a mana ability. */
public record PermanentHasNonManaActivatedAbilityPredicate() implements PermanentPredicate {
}
