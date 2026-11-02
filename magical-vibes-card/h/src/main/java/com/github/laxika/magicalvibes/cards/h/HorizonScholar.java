package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "59")
@CardRegistration(set = "THS", collectorNumber = "51")
public class HorizonScholar extends Card {

    public HorizonScholar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
    }
}
