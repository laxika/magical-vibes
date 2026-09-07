package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If damage would be dealt to this creature, put that many +1/+1
 * counters on it instead."
 * (e.g. Phytohydra)
 */
public record PreventDamageAndAddPlusCountersEffect() implements CardEffect {
}
