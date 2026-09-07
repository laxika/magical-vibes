package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "TSP", collectorNumber = "200")
public class HerdGnarr extends Card {

    public HerdGnarr() {
        // Whenever another creature you control enters, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new BoostSelfEffect(2, 2));
    }
}
