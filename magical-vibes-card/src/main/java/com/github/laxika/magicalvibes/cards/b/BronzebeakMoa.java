package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "DGM", collectorNumber = "60")
public class BronzebeakMoa extends Card {

    public BronzebeakMoa() {
        // Whenever another creature you control enters, this creature gets +3/+3 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new BoostSelfEffect(3, 3));
    }
}
