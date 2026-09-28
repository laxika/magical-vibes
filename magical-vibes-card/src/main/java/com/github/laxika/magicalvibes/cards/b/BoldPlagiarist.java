package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;

@CardRegistration(set = "C21", collectorNumber = "37")
public class BoldPlagiarist extends Card {

    public BoldPlagiarist() {
        addEffect(EffectSlot.ON_OPPONENT_PUT_COUNTERS_ON_CREATURE_THEY_CONTROL,
                new PutSameCountersOnSourceEffect());
    }
}
