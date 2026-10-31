package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KHM", collectorNumber = "159")
public class Arachnoform extends Card {

    public Arachnoform() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.REACH, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
