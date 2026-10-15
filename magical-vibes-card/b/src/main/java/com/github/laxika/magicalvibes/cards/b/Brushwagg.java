package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MIR", collectorNumber = "208")
public class Brushwagg extends Card {

    public Brushwagg() {
        // Whenever this creature blocks or becomes blocked, it gets -2/+2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(-2, 2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(-2, 2));
    }
}
