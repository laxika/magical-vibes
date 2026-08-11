package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "68")
public class DebilitatingInjury extends Card {

    public DebilitatingInjury() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE));
    }
}
