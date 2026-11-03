package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "42")
public class SunstarLightsmith extends Card {

    public SunstarLightsmith() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new DrawCardEffect(1));
    }
}
