package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "VIS", collectorNumber = "59")
public class FallenAskari extends Card {

    public FallenAskari() {
        // Flanking is auto-loaded from Scryfall and handled by the engine.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
