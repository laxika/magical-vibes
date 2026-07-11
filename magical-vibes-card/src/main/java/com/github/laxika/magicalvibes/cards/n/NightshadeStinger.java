package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "LRW", collectorNumber = "132")
public class NightshadeStinger extends Card {

    public NightshadeStinger() {
        // Flying is auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
