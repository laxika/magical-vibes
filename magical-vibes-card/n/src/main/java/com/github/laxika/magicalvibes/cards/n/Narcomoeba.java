package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "47")
@CardRegistration(set = "FUT", collectorNumber = "54")
@CardRegistration(set = "MMA", collectorNumber = "55")
@CardRegistration(set = "SLZ", collectorNumber = "26")
@CardRegistration(set = "SLZ", collectorNumber = "147")
@CardRegistration(set = "SLZ", collectorNumber = "268")
public class Narcomoeba extends Card {

    public Narcomoeba() {
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, ObjectRelation.THIS), new MayEffect(
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                "Put Narcomoeba onto the battlefield?"));
    }
}
