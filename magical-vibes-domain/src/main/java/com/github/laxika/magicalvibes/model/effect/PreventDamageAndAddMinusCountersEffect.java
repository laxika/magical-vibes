package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to this creature, prevent that damage.
 * Put a -1/-1 counter on this creature for each 1 damage prevented this way."
 * (e.g. Phyrexian Hydra)
 */
public record PreventDamageAndAddMinusCountersEffect() implements CardEffect {
}
