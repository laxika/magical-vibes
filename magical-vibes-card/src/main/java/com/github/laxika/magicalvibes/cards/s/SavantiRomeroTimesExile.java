package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect;

@CardRegistration(set = "TMT", collectorNumber = "72")
@CardRegistration(set = "TMT", collectorNumber = "267")
public class SavantiRomeroTimesExile extends Card {

    public SavantiRomeroTimesExile() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
