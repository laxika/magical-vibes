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
    /**
     * Activate only before blockers are declared (any player's turn). Steps before
     * {@code DECLARE_BLOCKERS}, and only before the first combat phase's declare-blockers step when
     * a turn has multiple combats ({@code combatPhasesThisTurn <= 1}). Acidic Dagger.
     */
    BEFORE_BLOCKERS_DECLARED,
    ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED,
    /** Activate only during the declare blockers step (any player). General Jarkeld. */
    ONLY_DURING_DECLARE_BLOCKERS,
    /**
     * Activate only during the declare blockers step and only if at least one creature is blocking
     * this creature. Grizzled Wolverine.
     */
    ONLY_DURING_DECLARE_BLOCKERS_IF_BLOCKED,
    ONLY_DURING_COMBAT,
    /**
     * Activate only during a step that precedes the end of combat step (any player's turn).
     * Dwarven Sea Clan's "Activate only before the end of combat step."
     */
    ONLY_BEFORE_END_OF_COMBAT,
    OPPONENT_CONTROLS_FLYING_CREATURE,
    OPPONENT_CONTROLS_MORE_LANDS,
    ONLY_DURING_YOUR_TURN,
    ONLY_DURING_YOUR_UPKEEP,
    ONLY_DURING_ANY_UPKEEP,
    ONLY_DURING_YOUR_DRAW_STEP,
    /** Activate only during an upkeep step of a turn whose active player is not you. Trade Caravan. */
    ONLY_DURING_OPPONENTS_UPKEEP,
    /**
     * Activate only during a turn whose active player is not you. Ghost Town's
     * "Activate only if it's not your turn."
     */
    ONLY_DURING_OPPONENTS_TURN,
    /**
     * Activate only during a turn whose active player is not you, and only in a step that precedes
     * the combat phase. Maddening Imp's "Activate only during an opponent's turn and only before
     * combat."
     */
    ONLY_DURING_OPPONENTS_TURN_BEFORE_COMBAT,
    ONLY_WHILE_ATTACKING,
    /** Activate only if this creature is attacking or blocking. Sawback Manticore. */
    ONLY_WHILE_ATTACKING_OR_BLOCKING,
    ONLY_WHILE_CREATURE,
    POWER_4_OR_GREATER,
    RAID,
    SORCERY_SPEED
}
