package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents combat damage from a creature to the source creature and puts one
 * +1/+1 counter on the source for each creature whose damage is prevented.
 */
public record PreventCombatDamageToSelfAndAddPlusOneCounterEffect() implements CardEffect {
}
