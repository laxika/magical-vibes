package com.github.laxika.magicalvibes.model.effect;

/** Queues destruction of the targeted creature at the next end step if it attacked this turn. */
public record DestroyTargetIfAttackedAtEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
