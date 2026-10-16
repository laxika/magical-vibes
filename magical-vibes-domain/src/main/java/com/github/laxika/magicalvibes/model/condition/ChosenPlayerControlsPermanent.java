package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The player chosen as the source permanent's opponent on entry (Jihad's "the chosen player") controls
 * a permanent matching the predicate. Unlike {@link OpponentControlsPermanent}, the chosen player stays
 * fixed when control of the source changes.
 */
public record ChosenPlayerControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "chosen player controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "the chosen player controls no matching permanent";
    }
}
