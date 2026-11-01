package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "765")
@CardRegistration(set = "ECC", collectorNumber = "105")
@CardRegistration(set = "NCC", collectorNumber = "287")
@CardRegistration(set = "LCC", collectorNumber = "240")
@CardRegistration(set = "ONC", collectorNumber = "108")
@CardRegistration(set = "ZNC", collectorNumber = "65")
public class EvolutionSage extends Card {

    public EvolutionSage() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new ProliferateEffect());
    }
}
