package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "28")
@CardRegistration(set = "M20", collectorNumber = "25")
@CardRegistration(set = "SOI", collectorNumber = "25")
@CardRegistration(set = "GNT", collectorNumber = "14")
public class InspiringCaptain extends Card {

    public InspiringCaptain() {
        // When this creature enters, creatures you control get +1/+1 until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostAllOwnCreaturesEffect(1, 1));
    }
}
