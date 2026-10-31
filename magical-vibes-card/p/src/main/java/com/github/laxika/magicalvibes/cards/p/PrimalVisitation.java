package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "187")
public class PrimalVisitation extends Card {

    public PrimalVisitation() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE));
    }
}
