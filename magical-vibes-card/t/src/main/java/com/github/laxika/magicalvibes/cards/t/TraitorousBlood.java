package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "166")
@CardRegistration(set = "SIS", collectorNumber = "45")
public class TraitorousBlood extends Card {

    public TraitorousBlood() {
        target(TargetFilters.creature())
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
