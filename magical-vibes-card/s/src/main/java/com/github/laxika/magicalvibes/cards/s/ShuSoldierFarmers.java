package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "27")
@CardRegistration(set = "ME3", collectorNumber = "26")
public class ShuSoldierFarmers extends Card {

    public ShuSoldierFarmers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
    }
}
