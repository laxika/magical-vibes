package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "21")
public class IonasBlessing extends Card {

    public IonasBlessing() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.VIGILANCE), GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantAdditionalBlockEffect(1));
    }
}
