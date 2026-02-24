package com.github.laxika.magicalvibes.model.effect;

/**
 * Imprint — Look at the top N cards of your library, exile one face down,
 * then put the rest on the bottom of your library in any order.
 * The exiled card is imprinted on the source permanent.
 */
public record ImprintFromTopCardsEffect(int count) implements CardEffect {
}
