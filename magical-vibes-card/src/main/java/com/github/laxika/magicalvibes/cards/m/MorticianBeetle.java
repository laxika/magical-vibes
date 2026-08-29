package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ROE", collectorNumber = "117")
public class MorticianBeetle extends Card {

    public MorticianBeetle() {
        // Whenever a player sacrifices a creature, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ANY_CREATURE_SACRIFICED, new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Mortician Beetle?"));
    }
}
