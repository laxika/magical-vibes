package com.github.laxika.magicalvibes.model.filter;

/** Matches cards with at least one activated ability that is not a mana ability. */
public record CardHasNonManaActivatedAbilityPredicate() implements CardPredicate {
}
