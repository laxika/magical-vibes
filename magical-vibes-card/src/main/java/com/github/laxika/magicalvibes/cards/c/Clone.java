package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;

public class Clone extends Card {

    public Clone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyCreatureOnEnterEffect());
    }
}
