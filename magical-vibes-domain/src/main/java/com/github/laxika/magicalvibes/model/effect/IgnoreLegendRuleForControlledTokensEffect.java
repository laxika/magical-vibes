package com.github.laxika.magicalvibes.model.effect;

/** Static effect that exempts tokens controlled by the source's controller. */
public record IgnoreLegendRuleForControlledTokensEffect()
        implements ControlledTokensLegendRuleExemptionEffect {
}
