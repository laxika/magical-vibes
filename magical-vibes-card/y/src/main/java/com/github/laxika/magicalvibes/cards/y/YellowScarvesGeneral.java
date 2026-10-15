package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "PTK", collectorNumber = "126")
public class YellowScarvesGeneral extends Card {

    public YellowScarvesGeneral() {
        // Horsemanship — auto-loaded from Scryfall.
        //
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
