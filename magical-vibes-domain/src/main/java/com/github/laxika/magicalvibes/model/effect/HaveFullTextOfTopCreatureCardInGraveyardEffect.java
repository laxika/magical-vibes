package com.github.laxika.magicalvibes.model.effect;

/**
 * Static text-changing effect used by Volrath's Shapeshifter.
 *
 * <p>The controller's top graveyard card is copied only while it is a creature card. The
 * Shapeshifter's own abilities are retained by the engine when it builds the runtime copy.</p>
 */
public record HaveFullTextOfTopCreatureCardInGraveyardEffect() implements CardEffect {
}
