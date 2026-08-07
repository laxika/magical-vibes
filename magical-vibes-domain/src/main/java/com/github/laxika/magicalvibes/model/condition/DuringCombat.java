package com.github.laxika.magicalvibes.model.condition;

/**
 * Met while the game is in one of the five combat steps ({@code TurnStep.isCombatPhase()}).
 * Encodes the "dies during combat" wording (Mongrel Pack): the death trigger is put on the stack
 * and resolves in the same combat step the creature died in, so the current step still answers
 * whether the death happened during combat.
 */
public record DuringCombat() implements Condition {

    @Override
    public String conditionName() {
        return "during combat";
    }

    @Override
    public String conditionNotMetReason() {
        return "not during combat";
    }
}
