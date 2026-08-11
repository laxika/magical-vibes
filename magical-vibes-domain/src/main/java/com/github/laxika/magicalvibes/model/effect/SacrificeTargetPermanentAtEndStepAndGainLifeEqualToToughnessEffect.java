package com.github.laxika.magicalvibes.model.effect;

/**
 * At the next end step, sacrifices the targeted creature if the controller still controls it,
 * then that spell's controller gains life equal to the creature's toughness.
 */
public record SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
