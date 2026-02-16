package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

public class HighGround extends Card {

    public HighGround() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
    }
}
