package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "112")
@CardRegistration(set = "HOP", collectorNumber = "63")
public class RockslideElemental extends Card {

    public RockslideElemental() {
        // First strike (keyword, auto-loaded from Scryfall).
        // Whenever another creature dies, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                        "Put a +1/+1 counter on this creature?"));
    }
}
