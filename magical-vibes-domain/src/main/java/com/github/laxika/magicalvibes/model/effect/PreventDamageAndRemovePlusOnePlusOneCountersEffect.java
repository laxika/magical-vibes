package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to this creature, prevent that damage
 * and remove that many +1/+1 counters from it."
 * (e.g. Protean Hydra)
 */
public record PreventDamageAndRemovePlusOnePlusOneCountersEffect() implements CardEffect {
}
