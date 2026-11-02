package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "216")
@CardRegistration(set = "MM3", collectorNumber = "207")
@CardRegistration(set = "DDS", collectorNumber = "55")
@CardRegistration(set = "GK2", collectorNumber = "88")
@CardRegistration(set = "HA1", collectorNumber = "16")
@CardRegistration(set = "2X2", collectorNumber = "189")
public class BurningTreeEmissary extends Card {

    public BurningTreeEmissary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AwardManaEffect(ManaColor.RED));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AwardManaEffect(ManaColor.GREEN));
    }
}
