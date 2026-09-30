package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: nontoken creatures that would die are exiled with a blood counter
 * instead, tracked with the permanent carrying this effect.
 */
public record ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect() implements CardEffect {
}
