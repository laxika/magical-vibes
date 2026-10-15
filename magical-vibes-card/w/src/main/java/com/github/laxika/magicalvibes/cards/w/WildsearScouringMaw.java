package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "BLC", collectorNumber = "8")
@CardRegistration(set = "BLC", collectorNumber = "44")
public class WildsearScouringMaw extends Card {

    public WildsearScouringMaw() {
        addEffect(EffectSlot.GRANT_CASCADE_TO_ENCHANTMENT_FROM_HAND, new CascadeEffect());
    }
}
