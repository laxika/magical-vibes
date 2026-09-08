package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges the controller's hand and graveyard. Cards moved from the hand to the graveyard are
 * not discarded, so discard triggers do not fire.
 */
public record ExchangeHandAndGraveyardEffect() implements CardEffect {
}
