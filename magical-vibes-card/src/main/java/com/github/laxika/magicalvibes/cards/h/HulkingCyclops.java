package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "POR", collectorNumber = "134")
public class HulkingCyclops extends Card {

    public HulkingCyclops() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
