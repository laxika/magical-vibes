package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a player casts a spell, that player discards cards equal to the number of cards in
 * all graveyards with the same name as that spell. The spell-cast trigger collector snapshots the
 * spell name and queues a dynamic {@link DiscardEffect} for the caster.
 */
public record DiscardForSameNameCardsInGraveyardsOnSpellCastEffect() implements CardEffect {
}
