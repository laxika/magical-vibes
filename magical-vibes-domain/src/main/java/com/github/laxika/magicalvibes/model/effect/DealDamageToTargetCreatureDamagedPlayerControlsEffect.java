package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;

import java.util.UUID;

/**
 * Deals a dynamic amount of damage to a creature chosen from the player damaged by the source.
 * Binds the damaged player before selecting the creature target when the trigger is put on
 * the stack. The optional second constructor argument makes that player choose; otherwise
 * the ability controller chooses.
 */
public record DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount damage,
                                                                    boolean targetPlayerChooses)
        implements DamageDealingEffect, CombatDamageAmountAwareEffect, CombatDamageTriggerContextEffect,
        DamagedPlayerControlsTargetEffect {

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
    public PermanentPredicate predicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public UUID targetChooserId(UUID damagedPlayerId, UUID sourceControllerId) {
        return targetPlayerChooses ? damagedPlayerId : sourceControllerId;
    }

    @Override
    public CardEffect forDamagedPlayer(UUID playerId) {
        return new DealDamageToTargetCreatureEffect(damage, false,
                new PermanentControlledByPlayerPredicate(playerId));
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
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
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
