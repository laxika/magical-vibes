package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DanseMacabreEffect;

@CardRegistration(set = "AFC", collectorNumber = "22")
public class DanseMacabre extends Card {

    public DanseMacabre() {
        addEffect(EffectSlot.SPELL, new DanseMacabreEffect());
    }
}
