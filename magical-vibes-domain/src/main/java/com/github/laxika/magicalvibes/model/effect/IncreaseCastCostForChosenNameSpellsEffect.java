package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells cast by the enchanted player whose name equals the source permanent's
 * chosen name cost {@code amount} generic mana more to cast.
 *
 * <p>Kept as its own record because both the chosen name and the enchanted player live on the
 * source Aura rather than in a standalone card predicate.
 */
public record IncreaseCastCostForChosenNameSpellsEffect(int amount) implements CardEffect {
}
