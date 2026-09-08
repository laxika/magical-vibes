package com.github.laxika.magicalvibes.model.condition;

/** The source Case is solved. */
public record SourceIsSolved() implements Condition {

    @Override
    public String conditionName() {
        return "solved";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not solved";
    }
}
