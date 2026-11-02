package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "70")
@CardRegistration(set = "ANB", collectorNumber = "29")
public class Octoprophet extends Card {

    public Octoprophet() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
    }
}
