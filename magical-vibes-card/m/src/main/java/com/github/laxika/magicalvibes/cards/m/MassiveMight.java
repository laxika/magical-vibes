package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "208")
@CardRegistration(set = "DBL", collectorNumber = "475")
public class MassiveMight extends Card {

    public MassiveMight() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
