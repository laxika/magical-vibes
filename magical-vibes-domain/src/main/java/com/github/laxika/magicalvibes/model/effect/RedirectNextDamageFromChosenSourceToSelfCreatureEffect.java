package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to you this turn, that damage is dealt
 * to this creature instead." The source is chosen on resolution, and only damage that would be
 * dealt to the ability controller is redirected to the ability's source creature.
 */
public record RedirectNextDamageFromChosenSourceToSelfCreatureEffect() implements CardEffect {
}
