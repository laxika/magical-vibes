package com.github.laxika.magicalvibes.model.effect;

/** Registers a rest-of-turn trigger for combat damage dealt by a creature the controller controls to a player. */
public record RegisterDelayedCombatDamageEffect(CardEffect triggerEffect) implements CardEffect {
}
