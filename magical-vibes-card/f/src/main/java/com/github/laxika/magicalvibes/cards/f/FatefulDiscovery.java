package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "40")
public class FatefulDiscovery extends Card {

    public FatefulDiscovery() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL), new DrawCardEffect(1));
    }
}
