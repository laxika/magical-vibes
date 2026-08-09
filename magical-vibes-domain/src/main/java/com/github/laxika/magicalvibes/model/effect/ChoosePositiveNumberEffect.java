package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a positive integer during resolution and stores it on the source
 * permanent. The numeric prompt uses the engine's full positive integer range rather than
 * revealing any hidden hand-size bound.
 */
public record ChoosePositiveNumberEffect() implements CardEffect {
}
