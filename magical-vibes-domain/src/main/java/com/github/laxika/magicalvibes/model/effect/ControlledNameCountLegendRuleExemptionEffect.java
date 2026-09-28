package com.github.laxika.magicalvibes.model.effect;

/** Capability for a static effect whose legend-rule exemption depends on the source controller's
 * count of permanents with the same name. */
public interface ControlledNameCountLegendRuleExemptionEffect extends CardEffect {

    boolean exemptFromLegendRule(int sameNameCountControlledBySourceController);
}
