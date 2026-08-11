package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "ODY", collectorNumber = "86")
public class FerventDenial extends Card {

    public FerventDenial() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addCastingOption(new FlashbackCast("{5}{U}{U}"));
    }
}
