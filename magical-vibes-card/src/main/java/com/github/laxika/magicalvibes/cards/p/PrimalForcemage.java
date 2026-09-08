package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;

@CardRegistration(set = "TSP", collectorNumber = "212")
public class PrimalForcemage extends Card {

    public PrimalForcemage() {
        // Whenever another creature you control enters, that creature gets +3/+3 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BoostEnteringCreatureEffect(3, 3));
    }
}
