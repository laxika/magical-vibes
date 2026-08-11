package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a player casts a spell, counter that spell unless its caster pays an amount equal to
 * the number of matching cards in all graveyards. The spell-cast trigger collector snapshots the
 * cast spell's name into the resolved dynamic counter effect.
 */
public record CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect() implements CardEffect {
}
