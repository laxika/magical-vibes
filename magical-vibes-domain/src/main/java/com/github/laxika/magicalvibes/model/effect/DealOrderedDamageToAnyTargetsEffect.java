package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Deals fixed, ordered damage to multiple "any target" (creature or player) targets.
 * The i-th target receives damageAmounts.get(i) damage.
 * Used by cards like Cone of Flame (1 to first, 2 to second, 3 to third).
 */
public record DealOrderedDamageToAnyTargetsEffect(List<Integer> damageAmounts) implements CardEffect {
}
