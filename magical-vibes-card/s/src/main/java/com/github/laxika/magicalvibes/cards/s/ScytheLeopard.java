package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "188")
@CardRegistration(set = "DDR", collectorNumber = "19")
public class ScytheLeopard extends Card {

    public ScytheLeopard() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), BoostEffect.self(1, 1));
    }
}
