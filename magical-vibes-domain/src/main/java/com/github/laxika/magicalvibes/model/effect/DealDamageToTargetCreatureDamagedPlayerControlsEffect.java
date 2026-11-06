package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;


/**
 * Flaming Gambit's "that player or that planeswalker's controller may have this spell deal that damage to a creature
 * of their choice that player controls instead": deals {@code damage} to a creature the spell's target player (or the
 * target planeswalker's controller) controls, chosen as the spell resolves, by that player when
 * {@code targetPlayerChooses}, else by the spell's controller. A triggered ability's "deals damage to target creature
 * that player controls" is {@link DealDamageToTargetCreatureEffect} with a
 * {@code PermanentControlledByTriggeringPlayerPredicate}.
 */
public record DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage,
                                                                    boolean targetPlayerChooses)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage) {
        this(damage, false);
    }

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(int damage) {
        this(new Fixed(damage), false);
    }

    public DealDamageToTargetCreatureDamagedPlayerControlsEffect(int damage, boolean targetPlayerChooses) {
        this(new Fixed(damage), targetPlayerChooses);
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
