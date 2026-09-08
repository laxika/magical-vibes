package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Capability for a static effect that exempts permanents with a controlled subtype from the
 * legend rule.
 */
public interface ControlledSubtypeLegendRuleExemptionEffect extends CardEffect {

    CardSubtype exemptedSubtype();
}
