package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "223")
public class StonewoodInvocation extends Card {

    public StonewoodInvocation() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(5, 5))
                .addSpellEffect(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.TARGET));
    }
}
