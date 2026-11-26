package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "120")
public class Breach extends Card {

    public Breach() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FEAR, Subjects.target()));
    }
}
