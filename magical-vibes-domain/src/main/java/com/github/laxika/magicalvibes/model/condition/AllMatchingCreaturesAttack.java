package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Every permanent the controller controls that matches {@code filter} is attacking.
 * Vacuous when the controller controls no matching permanents (Visions FAQ for Mob Mentality:
 * the ability triggers when the only non-attackers are Walls). Filtered at declare-attackers
 * collection time for {@code ON_ALLY_CREATURES_ATTACK}; the wrapper is unwrapped onto the stack
 * so resolution does not re-check (not an intervening-if).
 */
public record AllMatchingCreaturesAttack(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "all matching creatures attack";
    }

    @Override
    public String conditionNotMetReason() {
        return "a matching creature did not attack";
    }
}
