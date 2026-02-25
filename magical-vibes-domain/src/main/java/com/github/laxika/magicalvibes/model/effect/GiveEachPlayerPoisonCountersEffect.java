package com.github.laxika.magicalvibes.model.effect;

/**
 * Give each player N poison counters (including the controller).
 * Used by cards like Ichor Rats whose ETB gives every player a poison counter.
 */
public record GiveEachPlayerPoisonCountersEffect(int amount) implements CardEffect {
}
