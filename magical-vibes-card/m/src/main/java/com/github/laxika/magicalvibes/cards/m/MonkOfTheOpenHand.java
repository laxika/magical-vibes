package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "25")
@CardRegistration(set = "HBG", collectorNumber = "96")
public class MonkOfTheOpenHand extends Card {

    public MonkOfTheOpenHand() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
