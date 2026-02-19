package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "53")
public class TrueBeliever extends Card {

    public TrueBeliever() {
        addEffect(EffectSlot.STATIC, new GrantControllerShroudEffect());
    }
}
