package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every card from the resolving ability controller's hand and tracks the cards with the
 * source permanent for a later source-linked effect.
 */
public record ExileControllerHandWithSourceEffect() implements CardEffect {
}
