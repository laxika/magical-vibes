package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals a dynamic amount of damage to a creature chosen from the player damaged by the source.
 * The target is selected during resolution, so the damaged player remains in the stack entry's
 * {@code targetId} rather than in this effect's target spec. The optional second constructor
 * argument makes that player choose; the default preserves the existing controller-choice
 * behavior for triggered abilities such as Spark Mage.
 */
public record DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage,
                                                                    boolean targetPlayerChooses)
        implements DamageDealingEffect, CombatDamageAmountAwareEffect {

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage) {
        this(damage, false);
    }

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(int damage) {
        this(new Fixed(damage), false);
    }

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage,
                                                                 boolean targetPlayerChooses) {
        this.damage = damage;
        this.targetPlayerChooses = targetPlayerChooses;
    }

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(int damage,
                                                                 boolean targetPlayerChooses) {
        this(new Fixed(damage), targetPlayerChooses);
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
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
