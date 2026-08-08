package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to target creature. The amount is a {@link DynamicAmount} evaluated at
 * resolution (fixed number, X paid, source's toughness, controlled permanent count, …).
 *
 * @param damage          the amount of damage to deal
 * @param unpreventable   when true, the damage can't be prevented (e.g. Combust)
 * @param targetPredicate optional extra restriction on the targeted creature (for example,
 *                        "target creature an opponent controls" or "target Spirit creature");
 *                        {@code null} for plain "target creature"
 */
public record DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable,
                                               PermanentPredicate targetPredicate)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureEffect(int damage) {
        this(new Fixed(damage), false, null);
    }

    public DealDamageToTargetCreatureEffect(int damage, boolean unpreventable) {
        this(new Fixed(damage), unpreventable, null);
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage) {
        this(damage, false, null);
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable) {
        this(damage, unpreventable, null);
    }

    public DealDamageToTargetCreatureEffect(int damage, PermanentPredicate targetPredicate) {
        this(new Fixed(damage), false, targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), targetPredicate);
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
