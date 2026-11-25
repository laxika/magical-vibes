package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "233")
@CardRegistration(set = "KHM", collectorNumber = "194")
@CardRegistration(set = "TDM", collectorNumber = "159")
@CardRegistration(set = "OTJ", collectorNumber = "181")
@CardRegistration(set = "STA", collectorNumber = "57")
@CardRegistration(set = "CMM", collectorNumber = "323")
public class SnakeskinVeil extends Card {

    public SnakeskinVeil() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.target()));
    }
}
