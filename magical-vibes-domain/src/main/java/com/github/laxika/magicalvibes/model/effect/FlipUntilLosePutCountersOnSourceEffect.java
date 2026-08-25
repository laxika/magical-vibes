package com.github.laxika.magicalvibes.model.effect;

/**
 * Flips coins until the controller loses a flip, then puts one +1/+1 counter on the source for
 * each flip won.
 */
public record FlipUntilLosePutCountersOnSourceEffect() implements CardEffect {
}
