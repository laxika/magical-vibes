package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "170")
public class MigratingKetradon extends Card {

    public MigratingKetradon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
        addCycling("{2}");
    }
}
