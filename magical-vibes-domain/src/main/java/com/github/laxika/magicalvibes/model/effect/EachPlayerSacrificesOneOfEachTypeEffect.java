package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses one permanent for each of the artifact, creature, enchantment, land, and
 * planeswalker types they can satisfy, then sacrifices the chosen permanents together.
 *
 * <p>A permanent with multiple listed types may satisfy more than one type, and choosing it more
 * than once has the same result as choosing it once. The effect is non-targeting and resolves its
 * choices in active-player order.
 */
public record EachPlayerSacrificesOneOfEachTypeEffect() implements CardEffect {
}
