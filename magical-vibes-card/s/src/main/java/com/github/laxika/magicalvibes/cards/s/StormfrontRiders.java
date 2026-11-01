package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnNControlledPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "20")
@CardRegistration(set = "DDF", collectorNumber = "17")
@CardRegistration(set = "TSR", collectorNumber = "46")
public class StormfrontRiders extends Card {

    public StormfrontRiders() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnNControlledPermanentsToHandEffect(2, new PermanentIsCreaturePredicate(), "creature"));
        addTrigger(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, Triggers.CREATURE, ObjectRelation.YOU_OWN),
                CreateTokenEffect.whiteSoldier(1));
    }
}
