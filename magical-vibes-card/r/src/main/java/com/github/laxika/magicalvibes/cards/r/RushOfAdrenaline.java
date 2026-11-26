package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "177")
@CardRegistration(set = "E02", collectorNumber = "28")
@CardRegistration(set = "SIR", collectorNumber = "174")
public class RushOfAdrenaline extends Card {

    public RushOfAdrenaline() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
