package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "191")
@CardRegistration(set = "INR", collectorNumber = "206")
@CardRegistration(set = "INR", collectorNumber = "414")
@CardRegistration(set = "PC2", collectorNumber = "66")
@CardRegistration(set = "PCA", collectorNumber = "66")
public class Lumberknot extends Card {

    public Lumberknot() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
