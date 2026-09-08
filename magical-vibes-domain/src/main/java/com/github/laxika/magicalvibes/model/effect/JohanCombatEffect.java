package com.github.laxika.magicalvibes.model.effect;

/**
 * Johan's optional combat effect: Johan cannot attack, and the ability controller's creatures do
 * not tap when they attack while Johan remains untapped.
 */
public record JohanCombatEffect() implements PermanentLockEffect, AttackWithoutTappingPermissionEffect {

    @Override
    public boolean locksAttacking() {
        return true;
    }
}
