package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOS", collectorNumber = "149")
@CardRegistration(set = "SOS", collectorNumber = "296")
public class GerminationPracticum extends Card {

    public GerminationPracticum() {
        addSpellEffect(PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate()));
    }
}
