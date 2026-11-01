package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "306")
@CardRegistration(set = "RNA", collectorNumber = "12")
@CardRegistration(set = "ANB", collectorNumber = "10")
@CardRegistration(set = "PIP", collectorNumber = "162")
@CardRegistration(set = "PIP", collectorNumber = "690")
public class ImpassionedOrator extends Card {

    public ImpassionedOrator() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), new GainLifeEffect(1));
    }
}
