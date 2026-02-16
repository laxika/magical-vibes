package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

public class AngelOfMercy extends Card {

    public AngelOfMercy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
