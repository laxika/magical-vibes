package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ISD", collectorNumber = "83")
public class ThinkTwice extends Card {

    public ThinkTwice() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
