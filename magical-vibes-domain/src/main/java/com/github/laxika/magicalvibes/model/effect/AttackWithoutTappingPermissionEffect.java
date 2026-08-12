package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for a continuous effect that allows a controller's creatures to attack
 * without being tapped.
 */
public interface AttackWithoutTappingPermissionEffect extends CardEffect {

    /** Whether attacking creatures controlled by the effect's controller skip the tap action. */
    default boolean allowsAttackingWithoutTapping() {
        return true;
    }
}
