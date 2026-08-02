package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the player targeted by the spell or ability has no cards in hand — "if that player has
 * no cards in hand" (Nezumi Shortfang's discard ability). Reads the stack entry's target, so it is
 * the per-target counterpart of {@link AnOpponentHandEmpty} (any opponent) and
 * {@link ActivePlayerHandEmpty} (the active player). Evaluates to false when there is no target.
 */
public record TargetPlayerHandEmpty() implements Condition {

    @Override
    public String conditionName() {
        return "that player has no cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player still has cards in hand";
    }
}
