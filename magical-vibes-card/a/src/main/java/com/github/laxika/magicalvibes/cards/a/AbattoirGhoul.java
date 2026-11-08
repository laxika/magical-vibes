package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "85")
@CardRegistration(set = "DDQ", collectorNumber = "50")
public class AbattoirGhoul extends Card {

    public AbattoirGhoul() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new GainLifeEffect(new TriggeringPermanentToughness()));
    }
}
