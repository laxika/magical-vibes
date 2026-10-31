package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "17")
@CardRegistration(set = "THB", collectorNumber = "25")
public class IndomitableWill extends Card {

    public IndomitableWill() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE));
    }
}
