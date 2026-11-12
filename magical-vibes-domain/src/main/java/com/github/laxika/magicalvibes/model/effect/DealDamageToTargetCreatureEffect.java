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
 * @param damageSource    what deals the damage: the ability's own source, or the permanent the trigger event was
 *                        about ("have it deal damage", Farrel's Mantle's enchanted creature, CR 608.2h)
 */
public record DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable,
                                               PermanentPredicate targetPredicate, DamageSource damageSource)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable,
                                            PermanentPredicate targetPredicate) {
        this(damage, unpreventable, targetPredicate, DamageSource.ABILITY_SOURCE);
    }

    /** {@code damage} to target creature matching {@code targetPredicate}, dealt by the trigger event's permanent. */
    public static DealDamageToTargetCreatureEffect byTriggeringPermanent(DynamicAmount damage,
                                                                         PermanentPredicate targetPredicate) {
        return new DealDamageToTargetCreatureEffect(damage, false, targetPredicate, DamageSource.TRIGGERING_PERMANENT);
    }

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
