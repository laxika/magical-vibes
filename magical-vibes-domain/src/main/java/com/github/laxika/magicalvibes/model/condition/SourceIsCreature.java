package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is a creature. */
public record SourceIsCreature() implements Condition {

    @Override
    public String conditionName() {
        return "source is a creature";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not a creature";
    }
}
