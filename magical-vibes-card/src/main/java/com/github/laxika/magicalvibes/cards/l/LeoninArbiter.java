package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;

@CardRegistration(set = "SOM", collectorNumber = "14")
public class LeoninArbiter extends Card {

    public LeoninArbiter() {
        addEffect(EffectSlot.STATIC, new CantSearchLibrariesEffect());
    }
}
