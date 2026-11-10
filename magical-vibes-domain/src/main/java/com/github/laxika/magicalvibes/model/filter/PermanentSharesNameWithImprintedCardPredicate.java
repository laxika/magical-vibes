package com.github.laxika.magicalvibes.model.filter;

/**
 * The permanent has the same name as the card imprinted on the source while that card is still in exile ("a land with
 * the same name as the exiled card": Extraplanar Lens). Judged with the source's card from the filter context.
 */
public record PermanentSharesNameWithImprintedCardPredicate() implements PermanentPredicate {
}
