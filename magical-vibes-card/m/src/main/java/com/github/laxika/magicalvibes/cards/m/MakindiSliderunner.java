package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "148")
@CardRegistration(set = "PIO", collectorNumber = "145")
public class MakindiSliderunner extends Card {

    public MakindiSliderunner() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), BoostEffect.self(1, 1));
    }
}
