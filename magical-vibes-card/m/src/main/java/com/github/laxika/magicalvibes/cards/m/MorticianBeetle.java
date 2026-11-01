package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "117")
@CardRegistration(set = "MM3", collectorNumber = "78")
public class MorticianBeetle extends Card {

    public MorticianBeetle() {
        // Whenever a player sacrifices a creature, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.ANY), new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Mortician Beetle?"));
    }
}
