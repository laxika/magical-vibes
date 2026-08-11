package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever any player casts a spell, that player is dealt damage equal to twice the number of
 * cards in all graveyards with the same name as that spell. The spell-cast trigger collector
 * snapshots the spell name and resolves the damage through the normal damage effect.
 */
public record DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect() implements CardEffect {
}
