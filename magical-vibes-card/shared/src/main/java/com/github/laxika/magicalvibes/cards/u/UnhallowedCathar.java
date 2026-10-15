package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

public class UnhallowedCathar extends Card {

    public UnhallowedCathar() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
