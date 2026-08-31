package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "ONS", collectorNumber = "194")
public class ChargingSlateback extends Card {

    public ChargingSlateback() {
        addMorph("{4}{R}");
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
