package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "PTK", collectorNumber = "154")
public class TrainedCheetah extends Card {

    public TrainedCheetah() {
        // Whenever this creature becomes blocked, it gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));
    }
}
