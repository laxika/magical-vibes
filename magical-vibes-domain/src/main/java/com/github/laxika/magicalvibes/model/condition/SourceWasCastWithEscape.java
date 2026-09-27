package com.github.laxika.magicalvibes.model.condition;

/** True while the source permanent was cast using escape. */
public record SourceWasCastWithEscape() implements Condition {

    @Override
    public String conditionName() {
        return "was cast with escape";
    }

    @Override
    public String conditionNotMetReason() {
        return "it was not cast with escape";
    }
}
