package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot replacement effect: if a source would deal at least {@code threshold} damage to a
 * permanent or player this turn, it deals {@code replacementDamage} instead.
 */
public record ReplaceDamageAboveThresholdThisTurnEffect(int threshold, int replacementDamage)
        implements CardEffect {
}
