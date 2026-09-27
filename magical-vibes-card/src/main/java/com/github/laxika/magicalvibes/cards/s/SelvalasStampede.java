package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SelvalasStampedeEffect;

@CardRegistration(set = "TDC", collectorNumber = "269")
public class SelvalasStampede extends Card {

    public SelvalasStampede() {
        addEffect(EffectSlot.SPELL, new SelvalasStampedeEffect());
    }
}
