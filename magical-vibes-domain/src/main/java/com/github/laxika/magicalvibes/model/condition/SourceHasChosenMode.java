package com.github.laxika.magicalvibes.model.condition;

import java.util.Objects;

/** The source permanent has stored the given as-enters mode. */
public record SourceHasChosenMode(String mode) implements Condition {

    public SourceHasChosenMode {
        Objects.requireNonNull(mode, "mode");
    }

    @Override
    public String conditionName() {
        return "source has chosen " + mode;
    }

    @Override
    public String conditionNotMetReason() {
        return "source has not chosen " + mode;
    }
}
