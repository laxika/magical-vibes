package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ZEN", collectorNumber = "45")
public class CosisTrickster extends Card {

    public CosisTrickster() {
        addEffect(EffectSlot.ON_OPPONENT_SHUFFLES_LIBRARY,
                new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                        "put a +1/+1 counter on Cosi's Trickster"));
    }
}
