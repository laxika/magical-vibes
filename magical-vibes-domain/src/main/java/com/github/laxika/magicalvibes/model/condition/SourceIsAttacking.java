package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is currently attacking. */
public record SourceIsAttacking() implements Condition {

    @Override
    public String conditionName() {
        return "source attacking";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not attacking";
    }
}
