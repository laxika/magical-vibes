package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "88")
public class WaveWingElemental extends Card {

    public WaveWingElemental() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), BoostEffect.self(2, 2));
    }
}
