package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Gives the first target player control of the second target permanent, then queues a reflexive ability. */
public record TargetPlayerGainsControlOfTargetPermanentThenEffect(CardEffect thenEffect)
        implements ControlStealingEffect {

    public TargetPlayerGainsControlOfTargetPermanentThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }

    @Override
    public Duration controlDuration() {
        return Duration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
