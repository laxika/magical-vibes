package com.github.laxika.magicalvibes.model.condition;

/**
 * Met when the current combat phase is the first combat phase of the turn (CR — Finest Hour's
 * intervening "if it's the first combat phase of the turn"). Evaluated against
 * {@code GameData.combatPhasesThisTurn == 1}; guards additional-combat effects from looping across
 * the extra combat phases they create.
 */
public record FirstCombatPhase() implements Condition {

    @Override
    public String conditionName() {
        return "first combat phase";
    }

    @Override
    public String conditionNotMetReason() {
        return "not the first combat phase of the turn";
    }
}
