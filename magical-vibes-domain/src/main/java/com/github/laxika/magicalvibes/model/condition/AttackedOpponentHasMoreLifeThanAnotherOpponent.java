package com.github.laxika.magicalvibes.model.condition;

/**
 * Whether the attacked opponent has more life than at least one other opponent of the source
 * controller. The attacked opponent is supplied as the condition context target.
 */
public record AttackedOpponentHasMoreLifeThanAnotherOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "attacked opponent has more life than another opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "attacked opponent does not have more life than another opponent";
    }
}
