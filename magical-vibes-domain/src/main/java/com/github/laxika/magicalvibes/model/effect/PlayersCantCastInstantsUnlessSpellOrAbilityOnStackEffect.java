package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: players can't cast instant spells while the stack is empty. Used by Priority
 * Avenger.
 */
public record PlayersCantCastInstantsUnlessSpellOrAbilityOnStackEffect() implements CardEffect {
}
