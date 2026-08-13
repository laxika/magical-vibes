package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals a dynamic amount of damage to a creature chosen from the player damaged by the source.
 * The target is selected during resolution, so the damaged player remains in the stack entry's
 * {@code targetId} rather than in this effect's target spec.
 */
public record DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage) {
        this.damage = damage;
    }

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(int damage) {
        this(new Fixed(damage));
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
