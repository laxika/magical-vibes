package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "51")
public class TempestOfLight extends Card {

    public TempestOfLight() {
        addEffect(EffectSlot.SPELL, new DestroyAllEnchantmentsEffect());
    }
}
