package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to target player or planeswalker.
 * Unlike {@link DealDamageToTargetOpponentOrPlaneswalkerEffect}, any player may be
 * chosen (including the controller); planeswalker permanents are also valid targets.
 * The amount is any {@link DynamicAmount} evaluated at resolution — a {@link Fixed}
 * constant (Boggart Shenanigans) or a cost-snapshotted value such as an {@code XValue}
 * (Brion Stoutarm's sacrificed creature's power).
 *
 * @param amount the amount of damage to deal
 */
public record DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount amount) implements CardEffect {

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PLANESWALKER);
    }
}
