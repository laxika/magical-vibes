package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;

public class TrueBeliever extends Card {

    public TrueBeliever() {
        addEffect(EffectSlot.STATIC, new GrantControllerShroudEffect());
    }
}
