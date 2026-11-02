package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "167")
@CardRegistration(set = "ALL", collectorNumber = "65")
@CardRegistration(set = "A25", collectorNumber = "120")
@CardRegistration(set = "BTD", collectorNumber = "34")
@CardRegistration(set = "DKM", collectorNumber = "10")
@CardRegistration(set = "ME1", collectorNumber = "86")
public class BalduvianHorde extends Card {

    public BalduvianHorde() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeUnlessDiscardCardTypeEffect(null, true));
    }
}
