package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "THB", collectorNumber = "32")
public class PiousWayfarer extends Card {

    public PiousWayfarer() {
        // Constellation — Whenever an enchantment you control enters, target creature gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new BoostTargetCreatureEffect(1, 1));
    }
}
