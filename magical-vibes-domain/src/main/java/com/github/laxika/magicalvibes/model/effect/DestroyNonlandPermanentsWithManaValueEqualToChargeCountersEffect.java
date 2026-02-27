package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy each nonland permanent with mana value equal to the number of charge counters
 * on the source permanent. The charge counter count is snapshotted into xValue before
 * sacrifice so it survives self-sacrifice costs.
 *
 * <p>Used by Ratchet Bomb and similar cards.
 */
public record DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect() implements CardEffect {
}
