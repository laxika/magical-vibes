package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "158")
@CardRegistration(set = "M20", collectorNumber = "170")
public class FeralInvocation extends Card {

    public FeralInvocation() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE));
    }
}
