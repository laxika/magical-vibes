package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that allows the controller to cast one permanent spell of each permanent type
 * (creature, artifact, enchantment, planeswalker) from their graveyard during each of their turns.
 * Each permanent type has its own "once per turn" slot. If a card has multiple permanent types,
 * one type is chosen when casting it.
 * Used by: Muldrotha, the Gravetide.
 */
public record CastPermanentSpellsFromGraveyardEffect() implements CardEffect {
}
