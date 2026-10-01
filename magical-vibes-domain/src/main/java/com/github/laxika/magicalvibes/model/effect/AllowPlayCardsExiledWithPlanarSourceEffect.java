package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect for a plane that lets its controller play cards exiled by that plane
 * during their turn using their normal costs and timing permissions.
 */
public record AllowPlayCardsExiledWithPlanarSourceEffect() implements CardEffect {
}
