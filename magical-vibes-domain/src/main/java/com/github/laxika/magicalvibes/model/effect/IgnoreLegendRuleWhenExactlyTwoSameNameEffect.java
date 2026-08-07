package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC: "If there are exactly two permanents named ~ on the battlefield, the legend rule doesn't
 * apply to them." (Brothers Yamazaki.) The count spans every player's battlefield, so a third copy
 * anywhere on the board switches the exemption back off and the legend rule applies normally.
 */
public record IgnoreLegendRuleWhenExactlyTwoSameNameEffect() implements LegendRuleExemptionEffect {

    @Override
    public boolean exemptFromLegendRule(int sameNameCountOnBattlefield) {
        return sameNameCountOnBattlefield == 2;
    }
}
