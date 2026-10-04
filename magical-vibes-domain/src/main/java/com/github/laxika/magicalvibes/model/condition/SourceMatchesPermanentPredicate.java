package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Tests the source permanent's current characteristics, using its snapshot after it leaves. */
public record SourceMatchesPermanentPredicate(PermanentPredicate predicate) implements Condition {
    @Override
    public String conditionName() {
        return "source matches the required characteristics";
    }

    @Override
    public String conditionNotMetReason() {
        return "source does not match the required characteristics";
    }
}
