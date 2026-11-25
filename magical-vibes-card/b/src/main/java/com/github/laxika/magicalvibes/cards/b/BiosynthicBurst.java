package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "173")
public class BiosynthicBurst extends Card {

    public BiosynthicBurst() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target()))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
