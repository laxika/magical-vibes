package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The source blocks an attacker with enough blockers, all matching the given predicate. */
public record SourceBlocksWithAtLeastAndOnlyMatchingBlockers(
        int minimumBlockers,
        PermanentPredicate blockerPredicate) implements Condition {

    @Override
    public String conditionName() {
        return "blocking alongside at least " + minimumBlockers + " matching creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return "the blocking group does not meet the requirement";
    }
}
