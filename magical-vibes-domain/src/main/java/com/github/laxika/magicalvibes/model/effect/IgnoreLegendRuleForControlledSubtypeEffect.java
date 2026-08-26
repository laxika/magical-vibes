package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect that exempts permanents with a chosen subtype from the legend rule while the
 * source is on the battlefield.
 */
public record IgnoreLegendRuleForControlledSubtypeEffect(CardSubtype exemptedSubtype)
        implements ControlledSubtypeLegendRuleExemptionEffect {
}
