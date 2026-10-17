package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent is attacking a player (not a planeswalker or battle). */
public record SourceAttackingPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "source is attacking a player";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not attacking a player";
    }
}
