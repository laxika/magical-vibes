package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to target creature. The amount is a {@link DynamicAmount} evaluated at
 * resolution (fixed number, X paid, source's toughness, controlled permanent count, …).
 *
 * @param damage        the amount of damage to deal
 * @param unpreventable when true, the damage can't be prevented (e.g. Combust)
 */
public record DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable) implements CardEffect {

    public DealDamageToTargetCreatureEffect(int damage) {
        this(new Fixed(damage), false);
    }

    public DealDamageToTargetCreatureEffect(int damage, boolean unpreventable) {
        this(new Fixed(damage), unpreventable);
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage) {
        this(damage, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
