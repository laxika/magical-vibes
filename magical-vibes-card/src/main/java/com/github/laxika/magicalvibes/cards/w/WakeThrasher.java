package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "EVE", collectorNumber = "31")
public class WakeThrasher extends Card {

    public WakeThrasher() {
        // Whenever a permanent you control becomes untapped, this creature gets +1/+1 until end of turn.
        // Fires once per untapped permanent (including itself untapping during the untap step).
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_UNTAPPED, new BoostSelfEffect(1, 1));
    }
}
