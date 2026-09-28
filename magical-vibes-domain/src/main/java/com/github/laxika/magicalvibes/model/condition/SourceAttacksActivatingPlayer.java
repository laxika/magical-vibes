package com.github.laxika.magicalvibes.model.condition;

/** True when the source permanent is attacking the player activating its ability directly. */
public record SourceAttacksActivatingPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "this creature is attacking you";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature is not attacking you";
    }
}
