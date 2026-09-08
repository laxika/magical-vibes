package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "PLC", collectorNumber = "75")
public class MirriTheCursed extends Card {

    public MirriTheCursed() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
