package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "POR", collectorNumber = "135")
public class HulkingGoblin extends Card {

    public HulkingGoblin() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
