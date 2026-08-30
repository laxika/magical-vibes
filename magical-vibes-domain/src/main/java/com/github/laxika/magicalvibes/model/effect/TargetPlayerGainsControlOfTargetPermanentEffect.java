package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the first target player permanent control of the second target permanent.
 * An optional follow-up resolves immediately if control actually changes.
 */
public record TargetPlayerGainsControlOfTargetPermanentEffect(CardEffect thenEffect) implements ControlStealingEffect {

    public TargetPlayerGainsControlOfTargetPermanentEffect() {
        this(null);
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
