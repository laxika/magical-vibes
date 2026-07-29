package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent was dealt damage this turn (Wall of Resistance). Backed by
 * {@code GameData.permanentsDealtDamageThisTurn}, the same tracking that
 * {@code PermanentDealtDamageThisTurnPredicate} reads — any damage counts, combat or not, and the
 * flag survives the damage being removed at cleanup only for the turn it was dealt in.
 */
public record SelfWasDealtDamageThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "was dealt damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it wasn't dealt damage this turn";
    }
}
