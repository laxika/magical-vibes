package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.Keyword;

/** The permanent that caused the trigger has the given keyword. */
public record TriggeringPermanentHasKeyword(Keyword keyword) implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent has " + keyword.name().toLowerCase().replace("_", " ");
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent does not have " + keyword.name().toLowerCase().replace("_", " ");
    }
}
