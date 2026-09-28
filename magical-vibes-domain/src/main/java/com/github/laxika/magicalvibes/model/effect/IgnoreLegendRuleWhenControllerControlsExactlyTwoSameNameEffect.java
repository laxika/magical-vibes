package com.github.laxika.magicalvibes.model.effect;

/** STATIC: the legend rule doesn't apply to the source while its controller controls exactly two
 * permanents with the source's name. */
public record IgnoreLegendRuleWhenControllerControlsExactlyTwoSameNameEffect()
        implements ControlledNameCountLegendRuleExemptionEffect {

    @Override
    public boolean exemptFromLegendRule(int sameNameCountControlledBySourceController) {
        return sameNameCountControlledBySourceController == 2;
    }
}
