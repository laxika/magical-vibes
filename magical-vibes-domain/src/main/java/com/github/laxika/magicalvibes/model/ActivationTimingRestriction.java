package com.github.laxika.magicalvibes.model;

public enum ActivationTimingRestriction {
    CAST_NONCREATURE_SPELL_THIS_TURN,
    /** Activate only if you control three or more creatures with different powers (Coven). */
    COVEN,
    METALCRAFT,
    MORBID,
    /**
     * Activate only before attackers are declared (any player's turn). Steps before
     * {@code DECLARE_ATTACKERS}, and only before the first combat phase's declare-attackers step
     * when a turn has multiple combats ({@code combatPhasesThisTurn <= 1}). Norritt.
     */
    BEFORE_ATTACKERS_DECLARED,
    ONLY_BEFORE_ATTACKERS_DECLARED,
    ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED,
    /** Activate only during the declare blockers step (any player). General Jarkeld. */
    ONLY_DURING_DECLARE_BLOCKERS,
    ONLY_DURING_COMBAT,
    OPPONENT_CONTROLS_FLYING_CREATURE,
    OPPONENT_CONTROLS_MORE_LANDS,
    ONLY_DURING_YOUR_TURN,
    ONLY_DURING_YOUR_UPKEEP,
    ONLY_DURING_ANY_UPKEEP,
    ONLY_WHILE_ATTACKING,
    ONLY_WHILE_CREATURE,
    POWER_4_OR_GREATER,
    RAID,
    SORCERY_SPEED
}
