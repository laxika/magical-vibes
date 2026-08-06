package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SignalTheClansEffect;

@CardRegistration(set = "GTC", collectorNumber = "194")
public class SignalTheClans extends Card {

    public SignalTheClans() {
        addEffect(EffectSlot.SPELL, new SignalTheClansEffect());
    }
}
