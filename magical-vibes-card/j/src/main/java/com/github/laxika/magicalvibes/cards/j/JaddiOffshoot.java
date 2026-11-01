package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "176")
@CardRegistration(set = "IMA", collectorNumber = "171")
@CardRegistration(set = "TDC", collectorNumber = "260")
public class JaddiOffshoot extends Card {

    public JaddiOffshoot() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new GainLifeEffect(1));
    }
}
