package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ORI", collectorNumber = "108")
public class MalakirCullblade extends Card {

    public MalakirCullblade() {
        // Whenever a creature an opponent controls dies, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
