package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "MMQ", collectorNumber = "264")
public class RushwoodElemental extends Card {

    public RushwoodElemental() {
        // At the beginning of your upkeep, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Rushwood Elemental?"));
    }
}
