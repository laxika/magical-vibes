package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a player casts a spell, that player gains life equal to the number of cards in all
 * graveyards with the same name as that spell. The spell-cast trigger collector snapshots the
 * spell name into the resolved graveyard-count effect.
 */
public record GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect() implements CardEffect {
}
