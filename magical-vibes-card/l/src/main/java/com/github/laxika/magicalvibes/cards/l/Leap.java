package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STH", collectorNumber = "35")
public class Leap extends Card {

    public Leap() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
