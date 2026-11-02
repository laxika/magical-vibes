package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "123")
public class AlaniasPathmaker extends Card {

    public AlaniasPathmaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
