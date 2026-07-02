package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * The source permanent currently has the given keyword (accounting for temporary
 * keyword removal from activated abilities).
 */
public record SelfHasKeyword(Keyword keyword) implements Condition {

    @Override
    public String conditionName() {
        return "has " + keyword.name().toLowerCase().replace("_", " ");
    }

    @Override
    public String conditionNotMetReason() {
        return "does not have " + keyword.name().toLowerCase().replace("_", " ");
    }
}
