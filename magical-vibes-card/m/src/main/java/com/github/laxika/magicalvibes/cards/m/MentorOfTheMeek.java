package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "21")
@CardRegistration(set = "INR", collectorNumber = "34")
@CardRegistration(set = "INR", collectorNumber = "345")
@CardRegistration(set = "M19", collectorNumber = "27")
@CardRegistration(set = "2X2", collectorNumber = "17")
@CardRegistration(set = "PIO", collectorNumber = "27")
@CardRegistration(set = "C14", collectorNumber = "79")
@CardRegistration(set = "MOC", collectorNumber = "196")
@CardRegistration(set = "LTC", collectorNumber = "173")
@CardRegistration(set = "ONC", collectorNumber = "81")
@CardRegistration(set = "HOC", collectorNumber = "170")
@CardRegistration(set = "C16", collectorNumber = "69")
@CardRegistration(set = "VOC", collectorNumber = "93")
@CardRegistration(set = "SCD", collectorNumber = "28")
@CardRegistration(set = "E01", collectorNumber = "15")
public class MentorOfTheMeek extends Card {

    public MentorOfTheMeek() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new EnteringCreatureMaxPowerConditionalEffect(2,
                        new MayPayManaEffect("{1}",
                                new DrawCardEffect(1),
                                "Pay {1} to draw a card?")));
    }
}
