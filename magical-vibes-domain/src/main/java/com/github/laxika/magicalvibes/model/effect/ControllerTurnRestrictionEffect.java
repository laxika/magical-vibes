package com.github.laxika.magicalvibes.model.effect;

/**
 * Static controller-scoped restriction whose condition is based on the controller's actions this
 * turn. The engine evaluates the returned facts for battlefield and planar sources.
 */
public interface ControllerTurnRestrictionEffect extends CardEffect {

    default boolean preventsAttackingAfterSpellCast() {
        return false;
    }

    default boolean preventsCastingAfterAttack() {
        return false;
    }
}
