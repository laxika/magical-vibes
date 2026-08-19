package com.github.laxika.magicalvibes.model.effect;

/**
 * Static combat restriction: this creature can't attack or block unless its controller taps an
 * untapped creature that was not declared as an attacker or blocker in this combat.
 */
public record CantAttackOrBlockUnlessTapEffect() implements CombatTapCostEffect {

    @Override
    public int tapCount() {
        return 1;
    }
}
