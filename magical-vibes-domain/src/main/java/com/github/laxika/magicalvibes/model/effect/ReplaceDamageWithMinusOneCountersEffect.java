package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If damage would be dealt to this creature, put that many -1/-1
 * counters on it instead." Unlike {@link PreventDamageAndAddMinusCountersEffect} nothing is
 * prevented, so it still applies to damage that can't be prevented (CR 615.12).
 * (e.g. Lichenthrope)
 */
public record ReplaceDamageWithMinusOneCountersEffect() implements CardEffect {
}
