package com.github.laxika.magicalvibes.model.condition;

/**
 * Met while no spell or ability is on the stack: "players can't cast instant spells unless a spell or ability is on the
 * stack" restricts while it is met (Priority Avenger). A spell or ability that is resolving is still on the stack
 * (CR 608.2n), also while a spell is cast during its resolution (CR 608.2g).
 */
public record StackIsEmpty() implements Condition {

    @Override
    public String conditionName() {
        return "the stack is empty";
    }

    @Override
    public String conditionNotMetReason() {
        return "a spell or ability is on the stack";
    }
}
