package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "25")
@CardRegistration(set = "HBG", collectorNumber = "96")
public class MonkOfTheOpenHand extends Card {

    public MonkOfTheOpenHand() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
