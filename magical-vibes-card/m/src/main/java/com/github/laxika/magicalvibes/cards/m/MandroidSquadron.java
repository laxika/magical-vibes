package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "521")
@CardRegistration(set = "MSC", collectorNumber = "841")
public class MandroidSquadron extends Card {

    public MandroidSquadron() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
