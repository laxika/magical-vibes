package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player reveals their hand, chooses one card of each color from it, then discards all other
 * nonland cards. A multicolored card may satisfy more than one color choice.
 */
public record EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffect() implements CardEffect {
}
