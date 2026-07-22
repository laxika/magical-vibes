package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the caster's library until a basic land card is revealed. That
 * basic land is put into the caster's hand, and all other cards revealed this way are put into
 * the caster's graveyard. If the library is exhausted without revealing a basic land, every
 * revealed card goes to the graveyard.
 * <p>
 * Used by Hermit Druid.
 */
public record RevealUntilBasicLandToHandRestToGraveyardEffect() implements CardEffect {
}
