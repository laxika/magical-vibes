package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;

@CardRegistration(set = "ODY", collectorNumber = "22")
public class Embolden extends Card {

    public Embolden() {
        addEffect(EffectSlot.SPELL, new PreventDividedDamageEffect(4));
        addCastingOption(new FlashbackCast("{1}{W}"));
    }
}
