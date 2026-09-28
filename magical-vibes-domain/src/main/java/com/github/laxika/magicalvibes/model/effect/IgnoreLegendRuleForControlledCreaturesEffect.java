package com.github.laxika.magicalvibes.model.effect;

/** Static effect that exempts creatures controlled by the source's controller from the legend rule. */
public record IgnoreLegendRuleForControlledCreaturesEffect()
        implements ControlledCreaturesLegendRuleExemptionEffect {
}
