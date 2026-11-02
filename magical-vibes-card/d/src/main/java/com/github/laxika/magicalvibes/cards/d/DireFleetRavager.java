package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "104")
@CardRegistration(set = "OTC", collectorNumber = "132")
@CardRegistration(set = "LCC", collectorNumber = "192")
public class DireFleetRavager extends Card {

    public DireFleetRavager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachPlayerLosesFractionOfLifeRoundedUpEffect(3));
    }
}
