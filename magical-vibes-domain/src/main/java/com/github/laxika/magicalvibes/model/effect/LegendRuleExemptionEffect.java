package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for STATIC effects that switch off the legend rule (CR 704.5j) for the
 * permanent they are printed on. The legend rule check reads the FACT ("is this permanent exempt
 * right now?") instead of naming a concrete effect type.
 */
public interface LegendRuleExemptionEffect extends CardEffect {

    /**
     * Whether the source permanent is currently exempt from the legend rule.
     *
     * @param sameNameCountOnBattlefield how many permanents with the source's name are on the
     *                                   battlefield across <em>all</em> players
     */
    boolean exemptFromLegendRule(int sameNameCountOnBattlefield);
}
