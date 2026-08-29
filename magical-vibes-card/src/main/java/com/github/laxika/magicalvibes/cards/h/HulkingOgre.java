package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "UDS", collectorNumber = "87")
@CardRegistration(set = "S99", collectorNumber = "108")
public class HulkingOgre extends Card {

    public HulkingOgre() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
