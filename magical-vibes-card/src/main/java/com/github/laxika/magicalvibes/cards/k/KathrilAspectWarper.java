package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "10")
public class KathrilAspectWarper extends Card {

    private static final List<CounterType> KEYWORD_COUNTERS = List.of(
            CounterType.FLYING,
            CounterType.FIRST_STRIKE,
            CounterType.DOUBLE_STRIKE,
            CounterType.DEATHTOUCH,
            CounterType.HEXPROOF,
            CounterType.INDESTRUCTIBLE,
            CounterType.LIFELINK,
            CounterType.MENACE,
            CounterType.REACH,
            CounterType.TRAMPLE,
            CounterType.VIGILANCE
    );

    public KathrilAspectWarper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect(
                        KEYWORD_COUNTERS));
    }
}
