package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "80")
public class ClingingDarkness extends Card {

    public ClingingDarkness() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-4, -1, GrantScope.ENCHANTED_CREATURE));
    }
}
