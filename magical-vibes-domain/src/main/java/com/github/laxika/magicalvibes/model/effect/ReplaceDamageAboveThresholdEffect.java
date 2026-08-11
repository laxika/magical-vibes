package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a source would deal at least {@code threshold} damage to a
 * permanent or player, it deals {@code replacementDamage} instead.
 *
 * <p>The replacement applies to all sources and all damage recipients, including combat damage,
 * and is not prevention. Multiple instances are applied one at a time to the same damage event.
 */
public record ReplaceDamageAboveThresholdEffect(int threshold, int replacementDamage) implements CardEffect {
}
