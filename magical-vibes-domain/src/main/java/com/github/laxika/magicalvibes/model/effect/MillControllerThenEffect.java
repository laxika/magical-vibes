package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Mills cards from the controller's library, then queues a reflexive triggered ability. */
public record MillControllerThenEffect(int count, CardEffect thenEffect) implements CardEffect {

    public MillControllerThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
