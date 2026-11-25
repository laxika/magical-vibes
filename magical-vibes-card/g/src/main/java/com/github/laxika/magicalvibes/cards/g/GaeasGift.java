package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "182")
public class GaeasGift extends Card {

    public GaeasGift() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target()));
    }
}
