package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "GS1", collectorNumber = "26")
public class RecklessPangolin extends Card {

    public RecklessPangolin() {
        // Whenever this creature attacks, it gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(1, 1));
    }
}
