package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutRandomCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "234")
public class CrystallineGiant extends Card {

    private static final List<CounterType> COUNTER_TYPES = List.of(
            CounterType.FLYING,
            CounterType.FIRST_STRIKE,
            CounterType.DEATHTOUCH,
            CounterType.HEXPROOF,
            CounterType.LIFELINK,
            CounterType.MENACE,
            CounterType.REACH,
            CounterType.TRAMPLE,
            CounterType.VIGILANCE,
            CounterType.PLUS_ONE_PLUS_ONE
    );

    public CrystallineGiant() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new PutRandomCounterOnSourceEffect(COUNTER_TYPES));
    }
}
