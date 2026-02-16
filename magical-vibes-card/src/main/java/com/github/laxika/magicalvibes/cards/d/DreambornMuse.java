package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;

public class DreambornMuse extends Card {

    public DreambornMuse() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MillByHandSizeEffect());
    }
}
