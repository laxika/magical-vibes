package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "168")
@CardRegistration(set = "ZNR", collectorNumber = "193")
@CardRegistration(set = "IMA", collectorNumber = "174")
@CardRegistration(set = "SLD", collectorNumber = "1223")
@CardRegistration(set = "BLC", collectorNumber = "229")
public class LotusCobra extends Card {

    public LotusCobra() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new AwardAnyColorManaEffect());
    }
}
