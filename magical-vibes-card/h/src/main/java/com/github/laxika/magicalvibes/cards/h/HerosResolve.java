package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "24")
@CardRegistration(set = "TMP", collectorNumber = "23")
public class HerosResolve extends Card {

    public HerosResolve() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(1, 5, GrantScope.ENCHANTED_CREATURE));
    }
}
