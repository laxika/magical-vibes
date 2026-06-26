package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper for "one or more matching attackers" triggers.
 */
public record HasAttackerConditionalEffect(
        PermanentPredicate predicate,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "matching attacker";
    }

    @Override
    public String conditionNotMetReason() {
        return "no matching attacker";
    }
}
