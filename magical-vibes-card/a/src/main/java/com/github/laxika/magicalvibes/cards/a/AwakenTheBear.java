package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "129")
public class AwakenTheBear extends Card {

    public AwakenTheBear() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
