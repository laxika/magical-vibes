package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static effect that adds damage when a source controlled by its controller
 * would deal damage to an opponent or to a permanent an opponent controls.
 */
public interface ControllerOpponentDamageBonusEffect extends CardEffect {

    int amount();

    default boolean appliesToCombatDamage() {
        return true;
    }
}
