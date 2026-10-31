package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "142")
public class Spirespine extends Card {

    public Spirespine() {
        addCastingOption(new BestowCast("{4}{G}"));
        addStaticEffect(new MustBlockEachCombatEffect());

        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(4, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new MustBlockEachCombatEffect());
    }
}
