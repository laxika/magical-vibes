package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "130")
public class MinotaurSkullcleaver extends Card {

    public MinotaurSkullcleaver() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), BoostEffect.self(2, 0));
    }
}
