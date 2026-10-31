package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "63")
public class Buoyancy extends Card {

    public Buoyancy() {
        target(TargetFilters.creature());
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
    }
}
