package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/** Mills cards from the controller's library, then queues a reflexive ability if a condition is met. */
public record MillControllerThenIfConditionEffect(int count, Condition condition, CardEffect thenEffect)
        implements CardEffect {

    public MillControllerThenIfConditionEffect {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
