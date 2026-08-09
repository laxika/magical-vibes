package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker for Shared Fate: a draw is replaced by exiling face down the top
 * card of an opponent's library, tracked with the source enchantment and the player who exiled it.
 */
public record SharedFateDrawReplacementEffect() implements SharedFateDrawReplacement {
}
