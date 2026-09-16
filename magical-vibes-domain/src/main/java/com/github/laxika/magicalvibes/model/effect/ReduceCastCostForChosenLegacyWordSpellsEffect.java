package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells the source's controller casts that have the keyword or ability word chosen
 * by the source's Legacy pregame choice cost {@code amount} generic mana less to cast.
 */
public record ReduceCastCostForChosenLegacyWordSpellsEffect(int amount) implements CardEffect {
}
