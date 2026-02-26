package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token that's a copy of the card imprinted on the source permanent.
 * The token gains haste and is exiled at the beginning of the next end step.
 */
public record CreateTokenCopyOfImprintedCardEffect() implements CardEffect {
}
