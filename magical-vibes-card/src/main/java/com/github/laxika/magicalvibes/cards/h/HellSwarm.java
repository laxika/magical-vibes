package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "LEG", collectorNumber = "103")
public class HellSwarm extends Card {

    public HellSwarm() {
        // All creatures get -1/-0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, 0));
    }
}
