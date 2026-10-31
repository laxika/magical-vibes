package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "46")
public class FreshStart extends Card {

    public FreshStart() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-5, 0, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
