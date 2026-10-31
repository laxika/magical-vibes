package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "44")
public class CloakOfFeathers extends Card {

    public CloakOfFeathers() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        addSpellEffect(new DrawCardEffect(1));
    }
}
