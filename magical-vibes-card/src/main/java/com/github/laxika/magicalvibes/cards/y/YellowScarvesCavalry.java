package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "PTK", collectorNumber = "125")
public class YellowScarvesCavalry extends Card {

    public YellowScarvesCavalry() {
        // Horsemanship is auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
