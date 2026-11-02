package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "76")
@CardRegistration(set = "INR", collectorNumber = "362")
@CardRegistration(set = "AVR", collectorNumber = "67")
@CardRegistration(set = "DDQ", collectorNumber = "26")
@CardRegistration(set = "MM3", collectorNumber = "43")
@CardRegistration(set = "SIS", collectorNumber = "18")
@CardRegistration(set = "KHC", collectorNumber = "41")
public class MistRaven extends Card {

    public MistRaven() {
        // Flying (Scryfall keyword)
        // When this creature enters, return target creature to its owner's hand.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnToHandEffect.target());
    }
}
