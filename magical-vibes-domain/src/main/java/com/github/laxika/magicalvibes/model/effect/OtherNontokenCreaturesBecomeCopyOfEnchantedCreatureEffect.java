package com.github.laxika.magicalvibes.model.effect;

/**
 * "When this Aura enters attached to a creature, each other nontoken creature you control becomes a
 * copy of that creature." (Infinite Reflection)
 *
 * <p>The enchanted creature is re-derived from the source Aura's attachment at resolution, so the
 * effect needs no target. The copies are permanent (CR 707.2 copiable values) — they persist after
 * the Aura leaves the battlefield.
 */
public record OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect() implements CardEffect {
}
