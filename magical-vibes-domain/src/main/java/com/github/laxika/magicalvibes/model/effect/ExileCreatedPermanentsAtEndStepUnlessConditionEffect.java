package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.List;
import java.util.UUID;

/**
 * Registers the permanents created earlier in this resolution for a conditional next-end-step
 * exile. The list is populated only on the delayed copy of the effect.
 */
public record ExileCreatedPermanentsAtEndStepUnlessConditionEffect(
        List<UUID> permanentIds,
        Condition condition
) implements CardEffect {

    public ExileCreatedPermanentsAtEndStepUnlessConditionEffect(Condition condition) {
        this(null, condition);
    }
}
