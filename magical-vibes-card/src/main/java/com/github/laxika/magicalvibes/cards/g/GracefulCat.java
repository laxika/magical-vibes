package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "AKH", collectorNumber = "273")
public class GracefulCat extends Card {

    public GracefulCat() {
        // Whenever this creature attacks, it gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(1, 1));
    }
}
