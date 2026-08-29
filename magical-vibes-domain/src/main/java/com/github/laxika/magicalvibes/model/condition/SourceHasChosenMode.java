package com.github.laxika.magicalvibes.model.condition;

/** The source permanent previously chose the given named mode as it entered. */
public record SourceHasChosenMode(String mode) implements Condition {

    @Override
    public String conditionName() {
        return "source chose " + mode;
    }

    @Override
    public String conditionNotMetReason() {
        return "source did not choose " + mode;
    }
}
