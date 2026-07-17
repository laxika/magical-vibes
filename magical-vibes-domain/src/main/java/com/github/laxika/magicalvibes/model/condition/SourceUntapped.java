package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is untapped (e.g. Sphinx Sovereign's end-step branch). */
public record SourceUntapped() implements Condition {

    @Override
    public String conditionName() {
        return "source untapped";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is tapped";
    }
}
