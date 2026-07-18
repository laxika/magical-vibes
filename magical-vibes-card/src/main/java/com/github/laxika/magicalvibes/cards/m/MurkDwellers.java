package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "5ED", collectorNumber = "180")
@CardRegistration(set = "4ED", collectorNumber = "148")
public class MurkDwellers extends Card {

    public MurkDwellers() {
        // Whenever this creature attacks and isn't blocked, it gets +2/+0 until end of combat.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new BoostSelfEffect(2, 0));
    }
}
