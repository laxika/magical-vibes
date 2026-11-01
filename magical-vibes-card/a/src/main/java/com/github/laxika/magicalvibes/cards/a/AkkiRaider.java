package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "92")
public class AkkiRaider extends Card {

    public AkkiRaider() {
        // Whenever a land is put into a graveyard from the battlefield, this creature gets +1/+0 until end of turn.
        addTrigger(Triggers.dies(Triggers.LAND, ObjectRelation.ANY), new BoostSelfEffect(1, 0));
    }
}
