package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M13", collectorNumber = "74")
public class TricksOfTheTrade extends Card {

    public TricksOfTheTrade() {
        // Enchant creature
        target(TargetFilters.creature());
        // Enchanted creature gets +2/+0 and can't be blocked.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
