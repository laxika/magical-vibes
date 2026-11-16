package com.github.laxika.magicalvibes.model.effect;

/**
 * At the next end step, sacrifices the targeted creature if the controller still controls it,
 * then that spell's controller gains life equal to the creature's toughness.
 *
 * <p>The spell's form ({@code delayed} false) registers the delayed triggered ability; that ability's own effect
 * ({@code delayed} true) is the sacrifice itself, about the creature the ability records, and is not a target of the
 * card's.
 *
 * @param delayed whether this is the effect of the delayed triggered ability rather than the spell's
 */
public record SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect(boolean delayed)
        implements CardEffect {

    public SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect() {
        this(false);
    }
}
