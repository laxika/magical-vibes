package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller of the targeted permanent may sacrifice it or take damage.
 *
 * @param damage the damage dealt to the controller when they decline to sacrifice
 * @param targetIsDamageSource whether the targeted permanent is the source of the damage
 * @param targetRestriction the permanents that may be targeted (Star Athlete: nonland; Enchanter's
 *                          Bane: enchantment)
 */
public record TargetPermanentControllerMaySacrificeOrDamageEffect(
        DynamicAmount damage, boolean targetIsDamageSource, PermanentPredicate targetRestriction)
        implements DamageDealingEffect {

    public TargetPermanentControllerMaySacrificeOrDamageEffect(DynamicAmount damage, boolean targetIsDamageSource) {
        this(damage, targetIsDamageSource, new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    public TargetPermanentControllerMaySacrificeOrDamageEffect(int damage) {
        this(new Fixed(damage), false);
    }

    public TargetPermanentControllerMaySacrificeOrDamageEffect(DynamicAmount damage) {
        this(damage, false);
    }

    /** Uses the nonland targeted permanent as the source of the damage. */
    public static TargetPermanentControllerMaySacrificeOrDamageEffect withTargetAsDamageSource(
            DynamicAmount damage) {
        return new TargetPermanentControllerMaySacrificeOrDamageEffect(damage, true);
    }

    /** Uses the targeted permanent, restricted by {@code targetRestriction}, as the damage source. */
    public static TargetPermanentControllerMaySacrificeOrDamageEffect withTargetAsDamageSource(
            DynamicAmount damage, PermanentPredicate targetRestriction) {
        return new TargetPermanentControllerMaySacrificeOrDamageEffect(damage, true, targetRestriction);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetRestriction);
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
        return !targetIsDamageSource;
    }
}
