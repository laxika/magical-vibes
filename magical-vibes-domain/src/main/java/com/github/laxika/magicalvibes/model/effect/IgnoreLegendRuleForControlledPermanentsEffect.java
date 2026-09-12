package com.github.laxika.magicalvibes.model.effect;

/** Static effect that exempts all permanents controlled by the source's controller. */
public record IgnoreLegendRuleForControlledPermanentsEffect()
        implements ControlledPermanentsLegendRuleExemptionEffect {
}
