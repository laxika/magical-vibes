package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "45")
@CardRegistration(set = "SNC", collectorNumber = "44")
@CardRegistration(set = "ZNC", collectorNumber = "26")
public class FaerieVandal extends Card {

    public FaerieVandal() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
