package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "145")
@CardRegistration(set = "DDH", collectorNumber = "3")
@CardRegistration(set = "SLD", collectorNumber = "764")
@CardRegistration(set = "C14", collectorNumber = "194")
@CardRegistration(set = "LTC", collectorNumber = "242")
@CardRegistration(set = "CMA", collectorNumber = "106")
public class EssenceWarden extends Card {

    public EssenceWarden() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER), new GainLifeEffect(1));
    }
}
