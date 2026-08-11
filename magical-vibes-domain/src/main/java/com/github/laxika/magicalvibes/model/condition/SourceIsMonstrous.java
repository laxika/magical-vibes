package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is monstrous. */
public record SourceIsMonstrous() implements Condition {

    @Override
    public String conditionName() {
        return "monstrous";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not monstrous";
    }
}
