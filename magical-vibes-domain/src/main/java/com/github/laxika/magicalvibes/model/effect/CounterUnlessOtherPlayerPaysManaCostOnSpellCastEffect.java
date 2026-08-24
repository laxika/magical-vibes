package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a player casts a spell, another player may pay that spell's mana cost to counter it.
 * The spell-cast trigger collector snapshots the spell's mana cost and its chosen X value.
 */
public record CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect() implements CardEffect {
}
