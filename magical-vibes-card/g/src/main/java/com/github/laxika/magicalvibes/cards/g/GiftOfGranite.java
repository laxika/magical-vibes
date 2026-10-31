package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "7")
public class GiftOfGranite extends Card {

    public GiftOfGranite() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(0, 2, GrantScope.ENCHANTED_CREATURE));
    }
}
