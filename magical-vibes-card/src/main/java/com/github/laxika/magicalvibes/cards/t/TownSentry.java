package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "25")
public class TownSentry extends Card {

    public TownSentry() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(0, 2));
    }
}
