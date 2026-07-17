package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ALA", collectorNumber = "85")
public class ScavengerDrake extends Card {

    public ScavengerDrake() {
        // Flying (keyword, auto-loaded from Scryfall).
        // Whenever another creature dies, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                        "Put a +1/+1 counter on this creature?"));
    }
}
