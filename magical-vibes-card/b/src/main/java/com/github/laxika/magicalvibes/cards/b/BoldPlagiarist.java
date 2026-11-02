package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "37")
public class BoldPlagiarist extends Card {

    public BoldPlagiarist() {
        addTrigger(Triggers.opponentPutsCountersOnCreatureTheyControl(),
                new PutSameCountersOnSourceEffect());
    }
}
