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
public record DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable)
        implements DamageDealingEffect {

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
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
