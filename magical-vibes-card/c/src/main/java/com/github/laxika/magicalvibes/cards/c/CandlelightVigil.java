package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "3")
public class CandlelightVigil extends Card {

    public CandlelightVigil() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ENCHANTED_CREATURE));
    }
}
