package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to target player or planeswalker AND each creature
 * that player or that planeswalker's controller controls.
 * The amount is any {@link DynamicAmount} evaluated at resolution — a {@link Fixed}
 * constant (Chandra Nalaar's ultimate, Flame Wave) or a cost-snapshotted {@code XValue}
 * (Lavalanche).
 *
 * @param amount the amount of damage to deal
 */
public record DealDamageToTargetAndTheirCreaturesEffect(DynamicAmount amount) implements CardEffect {

    public DealDamageToTargetAndTheirCreaturesEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PLANESWALKER);
    }
}
