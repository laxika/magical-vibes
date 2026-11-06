package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share a card type with the permanent the resolving triggered ability's event was
 * about, as it last existed on the battlefield ({@link FilterContext#triggeringPermanentSnapshot()}): Martyr's
 * Bond's "each opponent sacrifices a permanent that shares a card type with it".
 */
public record PermanentSharesCardTypeWithTriggeringPermanentPredicate() implements PermanentPredicate {
}
