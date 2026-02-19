package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "82")
public class DreambornMuse extends Card {

    public DreambornMuse() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MillByHandSizeEffect());
    }
}
