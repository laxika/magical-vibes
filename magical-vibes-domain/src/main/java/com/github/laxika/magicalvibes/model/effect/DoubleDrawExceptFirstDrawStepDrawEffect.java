package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement effect: while a source with this effect is on the battlefield, if its
 * controller would draw a card — except the first card they draw in each of their own draw steps —
 * they draw two cards instead (Alhammarret's Archive).
 */
public record DoubleDrawExceptFirstDrawStepDrawEffect() implements CardEffect {
}
