package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share an artifact, creature, or enchantment card type with the
 * permanent carried in {@link FilterContext#sourcePermanentSnapshot()}.
 */
public record PermanentSharesCardTypeWithSourcePermanentPredicate() implements PermanentPredicate {
}
