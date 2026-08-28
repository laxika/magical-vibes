package com.github.laxika.magicalvibes.model.effect;

/**
 * Lets the controller cast up to {@code maxCount} face-up spells owned by opponents from exile
 * without paying their mana costs during this resolution.
 */
public record CastUpToNSpellsFromOpponentsExileEffect(int maxCount) implements CardEffect {
}
