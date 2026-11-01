package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "3")
public class AvenFarseer extends Card {

    public AvenFarseer() {
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.ANY),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
