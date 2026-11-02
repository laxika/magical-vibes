package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DDC", collectorNumber = "9")
@CardRegistration(set = "DVD", collectorNumber = "9")
@CardRegistration(set = "10E", collectorNumber = "2")
@CardRegistration(set = "INV", collectorNumber = "2")
@CardRegistration(set = "9ED", collectorNumber = "1")
@CardRegistration(set = "8ED", collectorNumber = "1")
@CardRegistration(set = "P02", collectorNumber = "8")
@CardRegistration(set = "S99", collectorNumber = "2")
@CardRegistration(set = "IMA", collectorNumber = "6")
public class AngelOfMercy extends Card {

    public AngelOfMercy() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
