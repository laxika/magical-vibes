package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to a creature chosen from the player dealt combat damage by the source.
 * The target is selected during resolution of a combat-damage may ability, so the damaged
 * player remains in the stack entry's {@code targetId} rather than in this effect's target spec.
 */
public record DealDamageToTargetCreatureDamagedPlayerControlsEffect(int damage)
        implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
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
