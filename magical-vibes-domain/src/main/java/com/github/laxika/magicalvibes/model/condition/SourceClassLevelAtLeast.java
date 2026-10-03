package com.github.laxika.magicalvibes.model.condition;

/** The source Class permanent has reached at least the specified level. */
public record SourceClassLevelAtLeast(int level) implements Condition {

    @Override
    public String conditionName() {
        return "Class level " + level + " or higher";
    }

    @Override
    public String conditionNotMetReason() {
        return "Class has not reached level " + level;
    }
}
