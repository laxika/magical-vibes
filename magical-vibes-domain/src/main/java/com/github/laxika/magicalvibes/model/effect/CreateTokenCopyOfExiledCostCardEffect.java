package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token that's a copy of the card exiled as part of an {@link ExileCardFromGraveyardCost}
 * with {@code imprintOnSource = true}. The exiled card is tracked via the source permanent's
 * imprinted card reference, set during cost payment.
 */
public record CreateTokenCopyOfExiledCostCardEffect() implements CardEffect {
}
