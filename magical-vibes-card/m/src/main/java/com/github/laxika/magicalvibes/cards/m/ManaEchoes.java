package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManaEchoesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "218")
@CardRegistration(set = "2XM", collectorNumber = "136")
@CardRegistration(set = "SLZ", collectorNumber = "64")
@CardRegistration(set = "SLZ", collectorNumber = "185")
@CardRegistration(set = "SLZ", collectorNumber = "306")
public class ManaEchoes extends Card {

    public ManaEchoes() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                new MayEffect(new ManaEchoesEffect(), "Add colorless mana?"));
    }
}
