package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger during resolution of an ability: if its source permanent dies later
 * this turn, return that card from its owner's graveyard to the battlefield transformed under the
 * ability controller's control.
 *
 * <p>The registration is made only while the source permanent is still on the battlefield, so an
 * ability whose source left before resolution cannot create the delayed trigger.</p>
 */
public record RegisterDelayedSelfReturnTransformedEffect() implements CardEffect {
}
