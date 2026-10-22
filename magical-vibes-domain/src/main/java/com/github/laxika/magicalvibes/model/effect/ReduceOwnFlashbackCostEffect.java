package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reduces the generic mana of this spell's flashback cost by the evaluated {@link DynamicAmount}.
 * Applies only when the spell is cast for its flashback cost; casting it from a graveyard by any other
 * means (e.g. Yawgmoth's Will) pays the normal cost unreduced.
 */
public record ReduceOwnFlashbackCostEffect(DynamicAmount amount) implements CardEffect {
}
