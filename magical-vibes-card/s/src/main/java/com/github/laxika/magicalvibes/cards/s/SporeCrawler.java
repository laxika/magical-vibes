package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "218")
@CardRegistration(set = "VOW", collectorNumber = "222")
@CardRegistration(set = "DBL", collectorNumber = "489")
public class SporeCrawler extends Card {

    public SporeCrawler() {
        // When this creature dies, draw a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
