package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "USG", collectorNumber = "182")
public class Dromosaur extends Card {

    public Dromosaur() {
        // Whenever this creature blocks or becomes blocked, it gets +2/-2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(2, -2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, -2));
    }
}
