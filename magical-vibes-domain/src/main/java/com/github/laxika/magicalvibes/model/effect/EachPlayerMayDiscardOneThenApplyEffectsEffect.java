package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may discard one card; players who discarded draw one card, then the supplied riders
 * resolve if a creature card or a noncreature card was discarded this way.
 */
public record EachPlayerMayDiscardOneThenApplyEffectsEffect(
        CardEffect creatureDiscardEffect,
        CardEffect nonCreatureDiscardEffect
) implements CardEffect {
}
