package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "P02", collectorNumber = "77")
public class LurkingNightstalker extends Card {

    public LurkingNightstalker() {
        // Whenever this creature attacks, it gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(2, 0));
    }
}
