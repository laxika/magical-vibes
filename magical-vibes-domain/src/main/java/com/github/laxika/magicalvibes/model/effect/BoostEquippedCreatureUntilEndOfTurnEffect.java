package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * The source equipment's equipped creature gets +powerBoost/+toughnessBoost until end of turn.
 * If the source equipment is not attached to a creature, the effect does nothing.
 *
 * <p>The dynamic amounts are evaluated relative to the equipped creature at resolution time, so a
 * {@link com.github.laxika.magicalvibes.model.amount.PermanentCount} counting attacking creatures
 * resolves correctly. Used to model equipment-granted triggered abilities that give the equipped
 * creature a (possibly dynamic) temporary boost, e.g. Veteran's Armaments' "Whenever this creature
 * attacks or blocks, it gets +1/+1 until end of turn for each attacking creature" (placed in the
 * {@code ON_ATTACK} and {@code ON_BLOCK} slots on the equipment). For a fixed boost that also grants
 * a keyword see {@link BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect}.
 */
public record BoostEquippedCreatureUntilEndOfTurnEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost
) implements CardEffect {
}
