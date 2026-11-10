package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "56")
public class LatNamAdept extends Card {

    public LatNamAdept() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
