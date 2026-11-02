package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GS1", collectorNumber = "9")
public class EarthOriginYak extends Card {

    public EarthOriginYak() {
        // When this creature enters, creatures you control get +1/+1 until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostAllOwnCreaturesEffect(1, 1));
    }
}
