package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "62")
@CardRegistration(set = "AKR", collectorNumber = "69")
public class NagaOracle extends Card {

    public NagaOracle() {
        // When this creature enters, surveil 3.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(3));
    }
}
