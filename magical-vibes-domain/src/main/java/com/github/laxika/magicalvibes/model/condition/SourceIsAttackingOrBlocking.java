package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is currently attacking or blocking. */
public record SourceIsAttackingOrBlocking() implements Condition {

    @Override
    public String conditionName() {
        return "source attacking or blocking";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is neither attacking nor blocking";
    }
}
