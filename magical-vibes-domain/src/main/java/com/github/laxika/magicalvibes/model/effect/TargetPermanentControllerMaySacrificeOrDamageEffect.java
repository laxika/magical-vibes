package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/** The controller of the targeted nonland permanent may sacrifice it or take damage. */
public record TargetPermanentControllerMaySacrificeOrDamageEffect(
        DynamicAmount damage, boolean targetIsDamageSource)
        implements DamageDealingEffect {

    public TargetPermanentControllerMaySacrificeOrDamageEffect(int damage) {
        this(new Fixed(damage), false);
    }

    public TargetPermanentControllerMaySacrificeOrDamageEffect(DynamicAmount damage) {
        this(damage, false);
    }

    /** Uses the targeted permanent as the source of the damage. */
    public static TargetPermanentControllerMaySacrificeOrDamageEffect withTargetAsDamageSource(
            DynamicAmount damage) {
        return new TargetPermanentControllerMaySacrificeOrDamageEffect(damage, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}
