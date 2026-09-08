package com.github.laxika.magicalvibes.model.condition;

/** A noncreature permanent under the controller's control was destroyed by an opponent this turn. */
public record NoncreaturePermanentDestroyedByOpponentThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a noncreature permanent you controlled was destroyed by an opponent this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no noncreature permanent you controlled was destroyed by an opponent this turn";
    }
}
