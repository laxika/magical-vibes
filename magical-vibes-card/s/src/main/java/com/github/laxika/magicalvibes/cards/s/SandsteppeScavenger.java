package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "200")
public class SandsteppeScavenger extends Card {

    public SandsteppeScavenger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BolsterEffect(2));
    }
}
