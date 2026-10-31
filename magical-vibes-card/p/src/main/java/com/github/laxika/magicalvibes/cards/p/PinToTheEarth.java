package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "45")
public class PinToTheEarth extends Card {

    public PinToTheEarth() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-6, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
