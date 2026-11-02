package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "72")
@CardRegistration(set = "TMT", collectorNumber = "267")
public class SavantiRomeroTimesExile extends Card {

    public SavantiRomeroTimesExile() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
