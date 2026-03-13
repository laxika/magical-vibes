package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "128")
public class BurningInquiry extends Card {

    public BurningInquiry() {
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(3));
        addEffect(EffectSlot.SPELL, new EachPlayerRandomDiscardEffect(3));
    }
}
