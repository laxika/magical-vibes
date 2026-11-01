package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "141")
public class ProteusMachine extends Card {

    public ProteusMachine() {
        addMorph("{0}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new SourceBecomesChosenSubtypeIndefinitelyEffect());
    }
}
