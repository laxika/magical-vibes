package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "14")
public class Saltskitter extends Card {

    public Saltskitter() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false));
    }
}
