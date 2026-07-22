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
        DynamicAmount damage, PermanentPredicate targetRestriction) implements DamageDealingEffect {

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage), null);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage, PermanentPredicate targetRestriction) {
        this(new Fixed(damage), targetRestriction);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage) {
        this(damage, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE_OR_PLANESWALKER, targetRestriction);
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
