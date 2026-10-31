package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "40")
@CardRegistration(set = "PIO", collectorNumber = "327")
public class FlitterstepEidolon extends Card {

    public FlitterstepEidolon() {
        addCastingOption(new BestowCast("{5}{U}"));

        addStaticEffect(new CantBeBlockedEffect());

        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new CantBeBlockedEffect());
    }
}
