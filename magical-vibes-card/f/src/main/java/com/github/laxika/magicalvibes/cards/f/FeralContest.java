package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MustBlockTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WWK", collectorNumber = "100")
public class FeralContest extends Card {

    public FeralContest() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        target(TargetFilters.creature())
                .addSpellEffect(new MustBlockTargetCreatureEffect(1, 0));
    }
}
