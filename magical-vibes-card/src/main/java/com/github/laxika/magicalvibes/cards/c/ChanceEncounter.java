package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

@CardRegistration(set = "ODY", collectorNumber = "182")
public class ChanceEncounter extends Card {

    public ChanceEncounter() {
        addEffect(EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP,
                new PutCountersOnSelfEffect(CounterType.LUCK));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCounterThreshold(10, CounterType.LUCK),
                        new WinGameEffect()));
    }
}
