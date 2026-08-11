package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a player casts a spell, that player creates one 1/1 green Squirrel token for each
 * card in all graveyards with the same name as that spell. The spell-cast trigger collector
 * snapshots the spell name into the resolved token effect.
 */
public record CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect() implements CardEffect {
}
