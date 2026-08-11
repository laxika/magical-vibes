package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "M13", collectorNumber = "16")
@CardRegistration(set = "M20", collectorNumber = "20")
public class GriffinProtector extends Card {

    public GriffinProtector() {
        // Whenever another creature you control enters, this creature gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 1));
    }
}
