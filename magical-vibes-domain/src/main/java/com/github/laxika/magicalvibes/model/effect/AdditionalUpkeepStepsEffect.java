package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/** Queues additional upkeep steps after the current combat phase. */
public record AdditionalUpkeepStepsEffect(DynamicAmount count) implements CombatDamageAmountAwareEffect {

    public AdditionalUpkeepStepsEffect {
        Objects.requireNonNull(count, "count");
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return count;
    }
}
