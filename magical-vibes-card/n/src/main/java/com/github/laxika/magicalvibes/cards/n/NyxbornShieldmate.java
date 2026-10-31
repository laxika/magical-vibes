package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "21")
public class NyxbornShieldmate extends Card {

    public NyxbornShieldmate() {
        addCastingOption(new BestowCast("{2}{W}"));

        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE));
    }
}
