package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "LEG", collectorNumber = "131")
public class AerathiBerserker extends Card {

    public AerathiBerserker() {
        // Rampage 3: whenever this creature becomes blocked, it gets +3/+3 until end of turn
        // for each creature blocking it beyond the first.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(3));
    }
}
