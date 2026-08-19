package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static combat restriction that is paid by tapping one or more creatures.
 * The combat services validate and pay the cost when attackers or blockers are declared.
 */
public interface CombatTapCostEffect extends CardEffect {

    /** Number of untapped creatures that must be tapped to pay this cost. */
    int tapCount();
}
