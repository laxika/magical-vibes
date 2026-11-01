package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOC", collectorNumber = "19")
@CardRegistration(set = "EOC", collectorNumber = "39")
public class SurgeConductor extends Card {

    public SurgeConductor() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new ProliferateEffect());
    }
}
