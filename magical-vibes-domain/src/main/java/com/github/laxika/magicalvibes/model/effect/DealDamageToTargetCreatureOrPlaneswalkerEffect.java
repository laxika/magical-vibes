package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to target creature or planeswalker (not player). The amount is a
 * {@link DynamicAmount} evaluated at resolution (fixed number, controlled permanent count, …).
 *
 * <p>An optional {@code targetRestriction} narrows the legal target (e.g. red only for
 * Chandra's Defeat); {@code null} means any creature or planeswalker is legal.
 */
public record DealDamageToTargetCreatureOrPlaneswalkerEffect(
        DynamicAmount damage, PermanentPredicate targetRestriction,
        boolean exileInsteadOfDie) implements DamageDealingEffect {

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage,
                                                           PermanentPredicate targetRestriction) {
        this(damage, targetRestriction, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage), null, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage, PermanentPredicate targetRestriction) {
        this(new Fixed(damage), targetRestriction, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage,
                                                           PermanentPredicate targetRestriction,
                                                           boolean exileInsteadOfDie) {
        this(new Fixed(damage), targetRestriction, exileInsteadOfDie);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage) {
        this(damage, null, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage,
                                                           boolean exileInsteadOfDie) {
        this(damage, null, exileInsteadOfDie);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creatureOrPlaneswalker(), targetRestriction);
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
