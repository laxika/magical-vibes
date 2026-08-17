package com.github.laxika.magicalvibes.model.condition;

/** True when every nonartifact creature on the battlefield shares at least one color. */
public record AllNonartifactCreaturesShareColor() implements Condition {

    @Override
    public String conditionName() {
        return "all nonartifact creatures share a color";
    }

    @Override
    public String conditionNotMetReason() {
        return "nonartifact creatures do not all share a color";
    }
}
