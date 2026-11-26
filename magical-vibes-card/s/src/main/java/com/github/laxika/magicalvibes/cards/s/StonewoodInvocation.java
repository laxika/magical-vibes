package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "223")
public class StonewoodInvocation extends Card {

    public StonewoodInvocation() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(5, 5))
                .addSpellEffect(new GrantKeywordEffect(Keyword.SHROUD, Subjects.target()));
    }
}
