package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "71")
@CardRegistration(set = "DDD", collectorNumber = "33")
@CardRegistration(set = "GVL", collectorNumber = "33")
public class Deathgreeter extends Card {

    public Deathgreeter() {
        // Whenever another creature dies, you may gain 1 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
