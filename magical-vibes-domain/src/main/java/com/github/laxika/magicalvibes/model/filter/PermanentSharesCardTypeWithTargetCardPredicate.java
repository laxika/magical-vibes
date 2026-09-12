package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share a permanent card type with the card identified by the target
 * card in the filter context.
 */
public record PermanentSharesCardTypeWithTargetCardPredicate() implements PermanentPredicate {
}
