package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "49")
@CardRegistration(set = "8ED", collectorNumber = "49")
@CardRegistration(set = "7ED", collectorNumber = "50")
@CardRegistration(set = "6ED", collectorNumber = "45")
@CardRegistration(set = "TPR", collectorNumber = "37")
public class StaunchDefenders extends Card {

    public StaunchDefenders() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
    }
}
