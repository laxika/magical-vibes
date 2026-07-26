package com.github.laxika.magicalvibes.ai;

/**
 * Internal scheduling signal for an AI decision.
 *
 * <p>These values describe engine facts, not wire-message shapes. The decision engine always
 * reads the authoritative live {@code GameData} when the delayed task runs.
 */
public enum AiDecisionKind {
    GAME_STATE,
    MULLIGAN,
    CARDS_TO_BOTTOM,
    ATTACKER_DECLARATION,
    BLOCKER_DECLARATION,
    INTERACTION,
    COMBAT_DAMAGE_ASSIGNMENT
}
